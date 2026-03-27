package com.healthcare.doctor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDto {
    private String id;
    private String username;
    private String email;
    private String name;
    private String specialty;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String qualifications;
    private String department;
    private String hospitalName;
    private String education;
    private String about;
    private String profileImageUrl;
    private Boolean isVerified;
    private Boolean enabled;
    
    /**
     * Returns isActive based on enabled status.
     * This maps the patient service's "enabled" field to what appointment service expects.
     */
    public Boolean getIsActive() {
        return enabled;
    }
    
    /**
     * Returns specialization which is the same as specialty.
     * This maps the patient service's "specialty" field to what appointment service expects.
     */
    public String getSpecialization() {
        return specialty;
    }
}
