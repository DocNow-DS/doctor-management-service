package com.healthcare.doctor.service;

import com.healthcare.doctor.model.PatientCarePlan;
import com.healthcare.doctor.model.PatientCarePlan.CarePlanStatus;
import com.healthcare.doctor.repository.PatientCarePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Patient Care Plans.
 *
 * All operations are doctor-scoped — a doctor can only manage plans they created.
 */
@Service
@RequiredArgsConstructor
public class PatientCarePlanService {

    private final PatientCarePlanRepository carePlanRepository;
    private final PatientServiceClient patientServiceClient;

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

        // Auto-set status
        plan.setStatus(CarePlanStatus.ACTIVE);

        // Auto-set timestamps
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        // Auto-calculate next visit date if nextVisitDays is provided
        if (plan.getNextVisitDays() != null && plan.getNextVisitDays() > 0) {
            plan.setNextVisitDate(LocalDate.now().plusDays(plan.getNextVisitDays()));
        }

        return carePlanRepository.save(plan);
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
        return carePlanRepository.findByDoctorId(doctorId);
    }

    /** Get all care plans for a specific patient (any doctor can view) */
    public List<PatientCarePlan> getCarePlansByPatient(String patientId) {
        return carePlanRepository.findByPatientId(patientId);
    }

    /** Get all care plans a specific doctor created for a specific patient */
    public List<PatientCarePlan> getCarePlansByDoctorAndPatient(String doctorId, String patientId) {
        if (!patientServiceClient.isUserValid(doctorId)) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }
        return carePlanRepository.findByDoctorIdAndPatientId(doctorId, patientId);
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
        return carePlanRepository.findByPatientIdAndStatus(patientId, CarePlanStatus.ACTIVE);
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
        }

        // Update next visit schedule — recalculate next visit date
        if (updatedPlan.getNextVisitDays() != null && updatedPlan.getNextVisitDays() > 0) {
            existing.setNextVisitDays(updatedPlan.getNextVisitDays());
            existing.setNextVisitDate(LocalDate.now().plusDays(updatedPlan.getNextVisitDays()));
        }

        // Always update timestamp
        existing.setUpdatedAt(LocalDateTime.now());

        return carePlanRepository.save(existing);
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
