package com.healthcare.doctor.dto;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        String patientId,
        String doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String consultationType,
        String notes,
        String doctorMessage,
        LocalDateTime proposedStartTime,
        LocalDateTime proposedEndTime,
        int progressPercent,
        String progressLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
