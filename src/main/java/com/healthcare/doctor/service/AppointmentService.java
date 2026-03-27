package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AppointmentServiceClient;
import com.healthcare.doctor.dto.AppointmentResponse;
import com.healthcare.doctor.dto.DoctorActionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for doctor-related appointment operations
 * Acts as a proxy to Appointment Management Service
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentServiceClient appointmentServiceClient;

    /**
     * Get all appointments for a doctor
     */
    public List<AppointmentResponse> getAppointmentsForDoctor(String doctorId, String status, String authorization) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        return appointmentServiceClient.getAppointmentsForDoctor(doctorId, status, authorization);
    }

    /**
     * Get pending appointments for a doctor
     */
    public List<AppointmentResponse> getPendingAppointments(String doctorId, String authorization) {
        return getAppointmentsForDoctor(doctorId, "PENDING", authorization);
    }

    /**
     * Get accepted appointments for a doctor
     */
    public List<AppointmentResponse> getAcceptedAppointments(String doctorId, String authorization) {
        return getAppointmentsForDoctor(doctorId, "ACCEPTED", authorization);
    }

    /**
     * Get a specific appointment
     */
    public AppointmentResponse getAppointment(String doctorId, String appointmentId, String authorization) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new IllegalArgumentException("Appointment ID is required");
        }
        return appointmentServiceClient.getAppointmentForDoctor(doctorId, appointmentId, authorization);
    }

    /**
     * Accept an appointment
     */
    public AppointmentResponse acceptAppointment(String doctorId, String appointmentId, String message, String authorization) {
        DoctorActionRequest request = new DoctorActionRequest(
                "ACCEPT",
                message,
                null,
                null
        );
        return appointmentServiceClient.performDoctorAction(doctorId, appointmentId, request, authorization);
    }

    /**
     * Decline an appointment
     */
    public AppointmentResponse declineAppointment(String doctorId, String appointmentId, String reason, String authorization) {
        DoctorActionRequest request = new DoctorActionRequest(
                "DECLINE",
                reason,
                null,
                null
        );
        return appointmentServiceClient.performDoctorAction(doctorId, appointmentId, request, authorization);
    }

    /**
     * Request appointment reschedule
     */
    public AppointmentResponse requestReschedule(
            String doctorId,
            String appointmentId,
            String message,
            java.time.LocalDateTime proposedStartTime,
            java.time.LocalDateTime proposedEndTime,
            String authorization) {
        DoctorActionRequest request = new DoctorActionRequest(
                "REQUEST_RESCHEDULE",
                message,
                proposedStartTime,
                proposedEndTime
        );
        return appointmentServiceClient.performDoctorAction(doctorId, appointmentId, request, authorization);
    }

    /**
     * Mark appointment as completed
     */
    public AppointmentResponse completeAppointment(String doctorId, String appointmentId, String authorization) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new IllegalArgumentException("Appointment ID is required");
        }
        return appointmentServiceClient.markAppointmentCompleted(doctorId, appointmentId, authorization);
    }
}
