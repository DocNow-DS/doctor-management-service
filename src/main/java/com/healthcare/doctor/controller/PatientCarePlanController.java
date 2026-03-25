package com.healthcare.doctor.controller;

import com.healthcare.doctor.model.PatientCarePlan;
import com.healthcare.doctor.service.PatientCarePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PatientCarePlan> createCarePlan(@RequestBody PatientCarePlan plan) {
        try {
            PatientCarePlan created = carePlanService.createCarePlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
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
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PatientCarePlan>> getCarePlansByPatient(@PathVariable String patientId) {
        List<PatientCarePlan> plans = carePlanService.getCarePlansByPatient(patientId);
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
     * GET /api/care-plans/patient/{patientId}/active
     *
     * Get only ACTIVE care plans for a specific patient.
     * Useful to show what's currently in effect for a patient.
     */
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PatientCarePlan>> getActiveCarePlansByPatient(@PathVariable String patientId) {
        List<PatientCarePlan> plans = carePlanService.getActiveCarePlansByPatient(patientId);
        return ResponseEntity.ok(plans);
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
