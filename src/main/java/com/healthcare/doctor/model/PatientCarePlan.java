package com.healthcare.doctor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the post-appointment clinical care plan a doctor creates for a patient.
 * Stored in the "patient_care_plans" MongoDB collection inside the Doctor Management service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "patient_care_plans")
public class PatientCarePlan {

    // ── Identity ──────────────────────────────────────────────────────────────

    @Id
    private String id;

    /** The doctor who created this plan */
    private String doctorId;

    /** The patient this plan belongs to */
    private String patientId;

    /**
     * Optional: the appointment that triggered this plan.
     * Leave null if creating a plan independently of an appointment.
     */
    private String appointmentId;

    // ── Medicine Plan ─────────────────────────────────────────────────────────

    /**
     * List of medicines with dosage, frequency, duration, and instructions.
     * Each entry is a MedicineItem embedded document.
     */
    private List<MedicineItem> medicines;

    // ── Allergies ─────────────────────────────────────────────────────────────

    /**
     * Known patient allergies relevant to this plan.
     * e.g. ["Penicillin", "Aspirin", "Latex"]
     */
    private List<String> allergies;

    // ── Consultation Notes ────────────────────────────────────────────────────

    /** Doctor's narrative notes for this visit / consultation */
    private String consultationNotes;

    // ── Pre-Visit Services ────────────────────────────────────────────────────

    /**
     * Lab tests / investigations the patient must complete before the next visit.
     * e.g. Blood test, Urine analysis, X-Ray, ECG.
     */
    private List<PreVisitService> preVisitServices;

    // ── Follow-up ─────────────────────────────────────────────────────────────

    /** "Return after X days" — used to calculate nextVisitDate automatically */
    private Integer nextVisitDays;

    /** Auto-calculated: createdAt.toLocalDate() + nextVisitDays */
    private LocalDate nextVisitDate;

    /** Auto-calculated total from medicine prices in this plan */
    private BigDecimal totalBill;

    // ── Status ────────────────────────────────────────────────────────────────

    /** Current lifecycle status of this care plan */
    private CarePlanStatus status;

    public enum CarePlanStatus {
        ACTIVE,     // Plan is currently in effect
        COMPLETED,  // Doctor marked care as done
        CANCELLED   // Plan was cancelled
    }

    // ── Timestamps ────────────────────────────────────────────────────────────

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
