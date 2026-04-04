package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.model.PatientCarePlan;
import com.healthcare.doctor.service.PatientCarePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Patient Care Plans.
 *
 * Base URL: /api/care-plans
 *
 * All endpoints require DOCTOR role.
 * Ownership-sensitive endpoints also verify the doctorId in the path
 * matches the authenticated doctor's ID.
 */
@RestController
@RequestMapping("/api/care-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PatientCarePlanController {

    private final PatientCarePlanService carePlanService;

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * POST /api/care-plans
     *
     * Doctor creates a new care plan for a patient after a consultation.
     * The plan includes medicines, dosage, frequency, duration, allergies,
     * consultation notes, pre-visit services, and next visit schedule.
     *
     * Body: PatientCarePlan JSON
     * Returns: created plan with auto-generated id, status=ACTIVE, nextVisitDate
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createCarePlan(@RequestBody PatientCarePlan plan,
                                            @AuthenticationPrincipal AuthResponse.User authenticatedUser) {
        try {
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }

            String doctorId = authenticatedUser.getId();
            if (doctorId == null || doctorId.isBlank()) {
                doctorId = authenticatedUser.getUsername();
            }

            if (doctorId == null || doctorId.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Unable to resolve authenticated doctor id"));
            }

            plan.setDoctorId(doctorId);
            PatientCarePlan created = carePlanService.createCarePlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // =========================================================================
    // READ
    // =========================================================================

    /**
     * GET /api/care-plans/{id}
     *
     * Fetch a single care plan by its MongoDB ID.
     * Any doctor can fetch by ID (use doctor-scoped endpoints for filtered access).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientCarePlan> getCarePlanById(@PathVariable String id) {
        return carePlanService.getCarePlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/care-plans/doctor/{doctorId}
     *
     * Get ALL care plans created by this doctor (all statuses).
     * Doctor can only view their own plans.
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<List<PatientCarePlan>> getCarePlansByDoctor(@PathVariable String doctorId) {
        try {
            List<PatientCarePlan> plans = carePlanService.getCarePlansByDoctor(doctorId);
            return ResponseEntity.ok(plans);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/care-plans/patient/{patientId}
     *
     * Get all care plans for a specific patient (across all doctors).
     * Useful for seeing a patient's full history.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<?> getCarePlansByPatient(@PathVariable String patientId,
                                                   @AuthenticationPrincipal AuthResponse.User authenticatedUser) {
        String effectivePatientId = resolveEffectivePatientId(patientId, authenticatedUser);
        if (effectivePatientId == null || effectivePatientId.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Unable to resolve patient access"));
        }
        List<PatientCarePlan> plans = carePlanService.getCarePlansByPatient(effectivePatientId);
        return ResponseEntity.ok(plans);
    }

    /**
     * GET /api/care-plans/doctor/{doctorId}/patient/{patientId}
     *
     * Get all care plans that THIS doctor created for THIS patient.
     * Most useful endpoint for the consultation screen — shows full history
     * of this doctor-patient relationship.
     */
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<List<PatientCarePlan>> getCarePlansByDoctorAndPatient(
            @PathVariable String doctorId,
            @PathVariable String patientId) {
        try {
            List<PatientCarePlan> plans = carePlanService.getCarePlansByDoctorAndPatient(doctorId, patientId);
            return ResponseEntity.ok(plans);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/care-plans/doctor/{doctorId}/active
     *
     * Get only ACTIVE care plans for this doctor.
     * Perfect for the doctor's dashboard "worklist" — patients still under care.
     */
    @GetMapping("/doctor/{doctorId}/active")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<List<PatientCarePlan>> getActiveCarePlansByDoctor(@PathVariable String doctorId) {
        try {
            List<PatientCarePlan> plans = carePlanService.getActiveCarePlansByDoctor(doctorId);
            return ResponseEntity.ok(plans);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/care-plans/doctor/{doctorId}/migrate-patient-ids
     *
     * One-time migration endpoint to replace legacy alias patient IDs
     * (username/email) with canonical patient IDs in this doctor's care plans.
     */
    @PostMapping("/doctor/{doctorId}/migrate-patient-ids")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<?> migratePatientIdsForDoctor(@PathVariable String doctorId) {
        try {
            int changed = carePlanService.migrateLegacyPatientIdsForDoctor(doctorId);
            return ResponseEntity.ok(Map.of(
                    "message", "Patient ID migration completed",
                    "updatedCount", changed
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/care-plans/patient/{patientId}/active
     *
     * Get only ACTIVE care plans for a specific patient.
     * Useful to show what's currently in effect for a patient.
     */
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<?> getActiveCarePlansByPatient(@PathVariable String patientId,
                                                         @AuthenticationPrincipal AuthResponse.User authenticatedUser) {
        String effectivePatientId = resolveEffectivePatientId(patientId, authenticatedUser);
        if (effectivePatientId == null || effectivePatientId.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Unable to resolve patient access"));
        }
        List<PatientCarePlan> plans = carePlanService.getActiveCarePlansByPatient(effectivePatientId);
        return ResponseEntity.ok(plans);
    }

    private String resolveEffectivePatientId(String requestedPatientId, AuthResponse.User authenticatedUser) {
        if (authenticatedUser == null) return null;
        String[] roles = authenticatedUser.getRoles();
        if (roles == null || roles.length == 0) return null;

        boolean isDoctorOrAdmin = java.util.Arrays.stream(roles)
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(r -> r.equals("DOCTOR") || r.equals("ADMIN"));
        if (isDoctorOrAdmin) return requestedPatientId;

        boolean isPatient = java.util.Arrays.stream(roles)
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(r -> r.equals("PATIENT"));
        if (!isPatient) return null;

        String authPatientId = authenticatedUser.getId();
        if (authPatientId == null || authPatientId.isBlank()) {
            authPatientId = authenticatedUser.getUsername();
        }
        return authPatientId;
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    /**
     * PUT /api/care-plans/{id}/doctor/{doctorId}
     *
     * Update an existing care plan.
     * Only the doctor who created the plan can update it.
     * - Send only the fields you want to change (null fields are ignored).
     * - nextVisitDate is automatically recalculated if nextVisitDays changes.
     */
    @PutMapping("/{id}/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<PatientCarePlan> updateCarePlan(
            @PathVariable String id,
            @PathVariable String doctorId,
            @RequestBody PatientCarePlan updatedPlan) {
        try {
            PatientCarePlan updated = carePlanService.updateCarePlan(id, doctorId, updatedPlan);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =========================================================================
    // STATUS TRANSITIONS
    // =========================================================================

    /**
     * PUT /api/care-plans/{id}/doctor/{doctorId}/complete
     *
     * Mark this care plan as COMPLETED.
     * Use this when the patient has finished the treatment and no further
     * follow-up is needed under this plan.
     */
    @PutMapping("/{id}/doctor/{doctorId}/complete")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<PatientCarePlan> completeCarePlan(
            @PathVariable String id,
            @PathVariable String doctorId) {
        try {
            PatientCarePlan plan = carePlanService.completeCarePlan(id, doctorId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/care-plans/{id}/doctor/{doctorId}/cancel
     *
     * Mark this care plan as CANCELLED.
     * Use this when the plan is no longer applicable
     * (e.g. patient switched doctors, wrong prescription).
     */
    @PutMapping("/{id}/doctor/{doctorId}/cancel")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<PatientCarePlan> cancelCarePlan(
            @PathVariable String id,
            @PathVariable String doctorId) {
        try {
            PatientCarePlan plan = carePlanService.cancelCarePlan(id, doctorId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * DELETE /api/care-plans/{id}/doctor/{doctorId}
     *
     * Permanently delete a care plan.
     * Only the doctor who created the plan can delete it.
     * Consider using CANCEL instead of delete to preserve history.
     */
    @DeleteMapping("/{id}/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<Void> deleteCarePlan(
            @PathVariable String id,
            @PathVariable String doctorId) {
        try {
            carePlanService.deleteCarePlan(id, doctorId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
