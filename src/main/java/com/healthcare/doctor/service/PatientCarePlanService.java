package com.healthcare.doctor.service;

import com.healthcare.doctor.client.NotificationServiceClient;
import com.healthcare.doctor.model.PatientCarePlan;
import com.healthcare.doctor.model.PatientCarePlan.CarePlanStatus;
import com.healthcare.doctor.repository.MedicineCatalogRepository;
import com.healthcare.doctor.repository.PatientCarePlanRepository;
import com.healthcare.doctor.repository.PreVisitServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business logic for Patient Care Plans.
 *
 * All operations are doctor-scoped — a doctor can only manage plans they created.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCarePlanService {

    private final PatientCarePlanRepository carePlanRepository;
    private final MedicineCatalogRepository medicineCatalogRepository;
    private final PreVisitServiceCatalogRepository preVisitServiceCatalogRepository;
    private final PatientServiceClient patientServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create a new care plan for a patient.
     *  - Sets status to ACTIVE automatically.
     *  - Calculates nextVisitDate from today + nextVisitDays.
     *  - Records createdAt timestamp.
     */
    public PatientCarePlan createCarePlan(PatientCarePlan plan) {
        if (plan.getDoctorId() == null || plan.getDoctorId().isBlank()) {
            throw new RuntimeException("Doctor id is required");
        }
        if (plan.getPatientId() == null || plan.getPatientId().isBlank()) {
            throw new RuntimeException("Patient id is required");
        }

        String canonicalPatientId = resolveCanonicalPatientId(plan.getPatientId());
        if (canonicalPatientId == null || canonicalPatientId.isBlank()) {
            throw new RuntimeException("Unable to resolve patient id");
        }
        plan.setPatientId(canonicalPatientId);

        // Auto-set status
        plan.setStatus(CarePlanStatus.ACTIVE);

        // Auto-set timestamps
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        // Auto-calculate next visit date if nextVisitDays is provided
        Long nextVisitDays = plan.getNextVisitDays();
        if (nextVisitDays != null && nextVisitDays > 0) {
            plan.setNextVisitDate(LocalDate.now().plusDays(nextVisitDays));
        }

        // Resolve and persist medicine prices from medicine catalog before save
        resolveMedicinePricesFromCatalog(plan);
        resolvePreVisitServicePricesFromCatalog(plan);

        // Auto-calculate total bill from medicine prices
        plan.setTotalBill(calculateTotalBill(plan));

        PatientCarePlan savedPlan = carePlanRepository.save(plan);
        
        // Send notification to patient about the new care plan
        sendCarePlanNotification(savedPlan);
        
        return savedPlan;
    }

    private void sendCarePlanNotification(PatientCarePlan carePlan) {
        try {
            String patientId = carePlan.getPatientId();
            String doctorId = carePlan.getDoctorId();
            String carePlanId = carePlan.getId();
            String appointmentId = carePlan.getAppointmentId();
            String consultationNotes = carePlan.getConsultationNotes();
            
            if (patientId != null && !patientId.isBlank()) {
                notificationServiceClient.sendCarePlanNotification(
                    patientId,
                    doctorId,
                    carePlanId,
                    appointmentId,
                    consultationNotes,
                    null  // Token is not available in this context
                );
                log.info("Care plan notification sent to patient {} for care plan {}", patientId, carePlanId);
            } else {
                log.warn("Cannot send care plan notification: patientId is null or blank for care plan {}", carePlanId);
            }
        } catch (Exception e) {
            log.error("Failed to send care plan notification: {}", e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't break care plan creation
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /** Get a single care plan by its ID */
    public Optional<PatientCarePlan> getCarePlanById(String id) {
        return carePlanRepository.findById(id);
    }

    /** Get all care plans created by a doctor */
    public List<PatientCarePlan> getCarePlansByDoctor(String doctorId) {
        if (!patientServiceClient.isUserValid(doctorId)) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }
        List<PatientCarePlan> plans = carePlanRepository.findByDoctorId(doctorId);
        return normalizeAndPersistPatientIds(plans);
    }

    /** Get all care plans for a specific patient (any doctor can view) */
    public List<PatientCarePlan> getCarePlansByPatient(String patientId) {
        String canonicalPatientId = resolveCanonicalPatientId(patientId);
        List<PatientCarePlan> plans = carePlanRepository.findByPatientId(canonicalPatientId);
        if (plans.isEmpty()) {
            // Attempt self-heal migration path for legacy alias-based patientId values.
            List<PatientCarePlan> allPlans = carePlanRepository.findAll();
            List<PatientCarePlan> normalized = normalizeAndPersistPatientIds(allPlans);
            plans = normalized.stream()
                    .filter(plan -> canonicalPatientId.equals(safeTrim(plan.getPatientId())))
                    .toList();
        }
        return normalizeAndPersistPatientIds(plans);
    }

    /** Get all care plans a specific doctor created for a specific patient */
    public List<PatientCarePlan> getCarePlansByDoctorAndPatient(String doctorId, String patientId) {
        if (!patientServiceClient.isUserValid(doctorId)) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }
        String canonicalPatientId = resolveCanonicalPatientId(patientId);
        List<PatientCarePlan> plans = carePlanRepository.findByDoctorIdAndPatientId(doctorId, canonicalPatientId);
        if (plans.isEmpty()) {
            // Attempt self-heal migration path for this doctor's legacy alias-based patientId values.
            List<PatientCarePlan> doctorPlans = carePlanRepository.findByDoctorId(doctorId);
            List<PatientCarePlan> normalized = normalizeAndPersistPatientIds(doctorPlans);
            plans = normalized.stream()
                    .filter(plan -> canonicalPatientId.equals(safeTrim(plan.getPatientId())))
                    .toList();
        }
        return normalizeAndPersistPatientIds(plans);
    }

    /** Get only ACTIVE care plans for a doctor (useful for dashboard worklist) */
    public List<PatientCarePlan> getActiveCarePlansByDoctor(String doctorId) {
        if (!patientServiceClient.isUserValid(doctorId)) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }
        return carePlanRepository.findByDoctorIdAndStatus(doctorId, CarePlanStatus.ACTIVE);
    }

    /** Get only ACTIVE care plans for a patient (useful for patient history view) */
    public List<PatientCarePlan> getActiveCarePlansByPatient(String patientId) {
        String canonicalPatientId = resolveCanonicalPatientId(patientId);
        List<PatientCarePlan> plans = carePlanRepository.findByPatientIdAndStatus(canonicalPatientId, CarePlanStatus.ACTIVE);
        if (plans.isEmpty()) {
            // Attempt self-heal migration path for legacy alias-based patientId values.
            List<PatientCarePlan> allPlans = carePlanRepository.findAll();
            List<PatientCarePlan> normalized = normalizeAndPersistPatientIds(allPlans);
            plans = normalized.stream()
                    .filter(plan -> canonicalPatientId.equals(safeTrim(plan.getPatientId())))
                    .filter(plan -> Boolean.TRUE.equals(plan.getStatus()))
                    .toList();
        }
        return normalizeAndPersistPatientIds(plans);
    }

    /**
     * One-time helper to migrate a doctor's historical plans from alias patient IDs
     * (username/email) into canonical patient ids.
     */
    public int migrateLegacyPatientIdsForDoctor(String doctorId) {
        if (!patientServiceClient.isUserValid(doctorId)) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }

        List<PatientCarePlan> plans = carePlanRepository.findByDoctorId(doctorId);
        List<String> beforeIds = plans.stream()
                .map(plan -> safeTrim(plan.getPatientId()))
                .toList();

        List<PatientCarePlan> normalized = normalizeAndPersistPatientIds(plans);
        int changed = 0;
        for (int i = 0; i < normalized.size() && i < beforeIds.size(); i++) {
            String before = beforeIds.get(i);
            String after = safeTrim(normalized.get(i).getPatientId());
            if (!before.equals(after)) {
                changed++;
            }
        }
        return changed;
    }

    private List<PatientCarePlan> normalizeAndPersistPatientIds(List<PatientCarePlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return plans;
        }

        Map<String, String> cache = new HashMap<>();
        List<PatientCarePlan> result = new ArrayList<>(plans.size());

        for (PatientCarePlan plan : plans) {
            if (plan == null) continue;

            String current = safeTrim(plan.getPatientId());
            String canonical = cache.computeIfAbsent(current, this::resolveCanonicalPatientId);

            if (!canonical.isBlank() && !canonical.equals(current)) {
                plan.setPatientId(canonical);
                plan.setUpdatedAt(LocalDateTime.now());
                plan = carePlanRepository.save(plan);
            }

            result.add(plan);
        }

        return result;
    }

    private String resolveCanonicalPatientId(String patientId) {
        String raw = safeTrim(patientId);
        if (raw.isBlank()) return raw;
        return safeTrim(patientServiceClient.resolveCanonicalPatientId(raw));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update an existing care plan.
     *  - Only the doctor who created the plan can update it.
     *  - Recalculates nextVisitDate if nextVisitDays changed.
     *  - Updates the updatedAt timestamp.
     */
    public PatientCarePlan updateCarePlan(String id, String doctorId, PatientCarePlan updatedPlan) {
        PatientCarePlan existing = carePlanRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Care plan not found or access denied"));

        // Update medicines
        if (updatedPlan.getMedicines() != null) {
            existing.setMedicines(updatedPlan.getMedicines());
            resolveMedicinePricesFromCatalog(existing);
        }

        // Update allergies
        if (updatedPlan.getAllergies() != null) {
            existing.setAllergies(updatedPlan.getAllergies());
        }

        // Update consultation notes
        if (updatedPlan.getConsultationNotes() != null) {
            existing.setConsultationNotes(updatedPlan.getConsultationNotes());
        }

        // Update pre-visit services
        if (updatedPlan.getPreVisitServices() != null) {
            existing.setPreVisitServices(updatedPlan.getPreVisitServices());
            resolvePreVisitServicePricesFromCatalog(existing);
        }

        // Update next visit schedule — recalculate next visit date
        Long updatedNextVisitDays = updatedPlan.getNextVisitDays();
        if (updatedNextVisitDays != null && updatedNextVisitDays > 0) {
            existing.setNextVisitDays(updatedNextVisitDays);
            existing.setNextVisitDate(LocalDate.now().plusDays(updatedNextVisitDays));
        }

        // Always update timestamp
        existing.setTotalBill(calculateTotalBill(existing));
        existing.setUpdatedAt(LocalDateTime.now());

        return carePlanRepository.save(existing);
    }

    private BigDecimal calculateTotalBill(PatientCarePlan plan) {
        BigDecimal medicineTotal = BigDecimal.ZERO;
        if (plan.getMedicines() != null && !plan.getMedicines().isEmpty()) {
            medicineTotal = plan.getMedicines().stream()
                .map(item -> item.getPrice() == null ? BigDecimal.ZERO : item.getPrice())
                .peek(price -> {
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Medicine price in care plan must be 0 or greater");
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal serviceTotal = BigDecimal.ZERO;
        if (plan.getPreVisitServices() != null && !plan.getPreVisitServices().isEmpty()) {
            serviceTotal = plan.getPreVisitServices().stream()
                    .map(item -> item.getPrice() == null ? BigDecimal.ZERO : item.getPrice())
                    .peek(price -> {
                        if (price.compareTo(BigDecimal.ZERO) < 0) {
                            throw new RuntimeException("Pre-visit service price in care plan must be 0 or greater");
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return medicineTotal.add(serviceTotal).stripTrailingZeros();
    }

    private void resolveMedicinePricesFromCatalog(PatientCarePlan plan) {
        if (plan.getMedicines() == null || plan.getMedicines().isEmpty()) {
            return;
        }

        List<com.healthcare.doctor.model.MedicineItem> resolved = new ArrayList<>();
        for (com.healthcare.doctor.model.MedicineItem item : plan.getMedicines()) {
            if (item == null) continue;

            String normalizedName = item.getMedicineName() == null ? "" : item.getMedicineName().trim();
            if (normalizedName.isBlank()) continue;

            item.setMedicineName(normalizedName);

            medicineCatalogRepository.findByNameIgnoreCaseAndActiveTrue(normalizedName)
                    .ifPresentOrElse(
                            catalogItem -> {
                                if (catalogItem.getPrice() == null) {
                                    throw new RuntimeException("Medicine catalog price is missing for: " + normalizedName);
                                }
                                item.setPrice(catalogItem.getPrice());
                            },
                            () -> {
                                if (item.getPrice() == null) {
                                    throw new RuntimeException("Price not found in medicine catalog for: " + normalizedName);
                                }
                                if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                                    throw new RuntimeException("Medicine price in care plan must be 0 or greater");
                                }
                            });

            resolved.add(item);
        }

        plan.setMedicines(resolved);
    }

    private void resolvePreVisitServicePricesFromCatalog(PatientCarePlan plan) {
        if (plan.getPreVisitServices() == null || plan.getPreVisitServices().isEmpty()) {
            return;
        }

        List<com.healthcare.doctor.model.PreVisitService> resolved = new ArrayList<>();
        for (com.healthcare.doctor.model.PreVisitService item : plan.getPreVisitServices()) {
            if (item == null) continue;

            String normalizedName = item.getServiceName() == null ? "" : item.getServiceName().trim();
            if (normalizedName.isBlank()) continue;

            item.setServiceName(normalizedName);

            preVisitServiceCatalogRepository.findByServiceNameIgnoreCaseAndActiveTrue(normalizedName)
                    .ifPresentOrElse(
                            catalogItem -> {
                                if (catalogItem.getPrice() == null) {
                                    throw new RuntimeException("Pre-visit service catalog price is missing for: " + normalizedName);
                                }
                                item.setPrice(catalogItem.getPrice());
                            },
                            () -> {
                                if (item.getPrice() == null) {
                                    throw new RuntimeException("Price not found in pre-visit service catalog for: " + normalizedName);
                                }
                                if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                                    throw new RuntimeException("Pre-visit service price in care plan must be 0 or greater");
                                }
                            });

            resolved.add(item);
        }

        plan.setPreVisitServices(resolved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS TRANSITIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Mark a care plan as COMPLETED.
     * Only the owning doctor can do this.
     */
    public PatientCarePlan completeCarePlan(String id, String doctorId) {
        PatientCarePlan plan = carePlanRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Care plan not found or access denied"));

        plan.setStatus(CarePlanStatus.COMPLETED);
        plan.setUpdatedAt(LocalDateTime.now());
        return carePlanRepository.save(plan);
    }

    /**
     * Mark a care plan as CANCELLED.
     * Only the owning doctor can do this.
     */
    public PatientCarePlan cancelCarePlan(String id, String doctorId) {
        PatientCarePlan plan = carePlanRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Care plan not found or access denied"));

        plan.setStatus(CarePlanStatus.CANCELLED);
        plan.setUpdatedAt(LocalDateTime.now());
        return carePlanRepository.save(plan);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Permanently delete a care plan.
     * Only the owning doctor can delete it.
     */
    public void deleteCarePlan(String id, String doctorId) {
        PatientCarePlan plan = carePlanRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Care plan not found or access denied"));

        carePlanRepository.deleteById(plan.getId());
    }
}
