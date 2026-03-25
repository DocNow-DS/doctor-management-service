package com.healthcare.doctor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded value object — NOT a separate MongoDB collection.
 * Represents a lab test / investigation the patient should do
 * before their next visit (e.g. blood test, urine analysis, X-Ray).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreVisitService {

    /** e.g. "Blood test", "Urine analysis", "X-Ray", "Ultrasound" */
    private String serviceName;

    /** Any extra instructions for this test, e.g. "Fasting required" */
    private String notes;
}
