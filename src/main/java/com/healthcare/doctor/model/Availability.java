package com.healthcare.doctor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "availability")
public class Availability {
    
    @Id
    private String id;
    private String doctorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String consultationType; // ONLINE, IN_PERSON, BOTH
    private Integer maxAppointments;
    private Boolean isActive;
    private String notes;
}
