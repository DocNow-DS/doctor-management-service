package com.healthcare.doctor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "prescriptions")
public class Prescription {
    
    @Id
    private String id;
    private String doctorId;
    private String patientId;
    private String appointmentId;
    private String diagnosis;
    private String medications;
    private String dosage;
    private String instructions;
    private Integer durationDays;
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private String notes;
    private String prescriptionUrl;
}
