package com.healthcare.doctor.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DoctorActionRequest(
        @JsonProperty("action")
        String action,
        String message,
        LocalDateTime proposedStartTime,
        LocalDateTime proposedEndTime) {
}
