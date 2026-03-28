package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AppointmentResponse;
import com.healthcare.doctor.dto.DoctorActionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Client to communicate with Appointment Management Service (default port 8080)
 */
@Component
@RequiredArgsConstructor
public class AppointmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${appointment.service.url:http://localhost:8080}")
    private String appointmentServiceBaseUrl;

    /**
     * Get all appointments for a doctor
     */
    public List<AppointmentResponse> getAppointmentsForDoctor(String doctorId, String status, String authorization) {
        try {
            String url = appointmentServiceBaseUrl + "/api/doctor/appointments";
            if (status != null && !status.isBlank()) {
                url += "?status=" + status;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Doctor-Id", doctorId);
            if (authorization != null && !authorization.isBlank()) {
                headers.set("Authorization", authorization);
            }

            ResponseEntity<AppointmentResponse[]> response = restTemplate.getForEntity(
                    url,
                    AppointmentResponse[].class);

            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get appointments from appointment service: " + e.getMessage());
        }
    }

    /**
     * Get a specific appointment for a doctor
     */
    public AppointmentResponse getAppointmentForDoctor(String doctorId, String appointmentId, String authorization) {
        try {
            String url = appointmentServiceBaseUrl + "/api/doctor/appointments/" + appointmentId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Doctor-Id", doctorId);
            if (authorization != null && !authorization.isBlank()) {
                headers.set("Authorization", authorization);
            }

            ResponseEntity<AppointmentResponse> response = restTemplate.getForEntity(
                    url,
                    AppointmentResponse.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get appointment from appointment service: " + e.getMessage());
        }
    }

    /**
     * Doctor action on appointment (ACCEPT, DECLINE, REQUEST_RESCHEDULE)
     */
    public AppointmentResponse performDoctorAction(
            String doctorId,
            String appointmentId,
            DoctorActionRequest request,
            String authorization) {
        try {
            String url = appointmentServiceBaseUrl + "/api/doctor/appointments/" + appointmentId + "/action";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Doctor-Id", doctorId);
            headers.set("Content-Type", "application/json");
            if (authorization != null && !authorization.isBlank()) {
                headers.set("Authorization", authorization);
            }

            HttpEntity<DoctorActionRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<AppointmentResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AppointmentResponse.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform doctor action: " + e.getMessage());
        }
    }

    /**
     * Mark appointment as completed
     */
    public AppointmentResponse markAppointmentCompleted(
            String doctorId,
            String appointmentId,
            String authorization) {
        try {
            String url = appointmentServiceBaseUrl + "/api/doctor/appointments/" + appointmentId + "/complete";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Doctor-Id", doctorId);
            if (authorization != null && !authorization.isBlank()) {
                headers.set("Authorization", authorization);
            }

            return restTemplate.patchForObject(
                    url,
                    new HttpEntity<>(headers),
                    AppointmentResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark appointment as completed: " + e.getMessage());
        }
    }
}
