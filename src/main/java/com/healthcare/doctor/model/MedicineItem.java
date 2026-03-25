package com.healthcare.doctor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded value object — NOT a separate MongoDB collection.
 * Used as a list item inside PatientCarePlan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineItem {

    /** e.g. "Amoxicillin", "Paracetamol" */
    private String medicineName;

    /** e.g. "500mg", "1 tablet", "10ml" */
    private String dosage;

    /** e.g. "Twice daily", "Every 8 hours", "Once at night" */
    private String frequency;

    /** Number of days the patient should take this medicine */
    private Integer durationDays;

    /** Extra instructions, e.g. "After meals", "With plenty of water" */
    private String instructions;
}
