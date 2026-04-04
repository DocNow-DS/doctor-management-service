package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AppointmentResponse;
import com.healthcare.doctor.dto.DoctorActionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Client to communicate with Appointment Management Service (default port 8080)
 */
@Component
public class AppointmentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceClient.class);

    private final RestTemplate restTemplate;

    public AppointmentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

            log.debug("Calling appointment service: GET {}", url);
            log.debug("Headers - X-Doctor-Id: {}, Authorization: {}", doctorId,
                    authorization != null ? "Bearer <token>" : "null");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<AppointmentResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AppointmentResponse[].class);

            log.debug("Appointment service response: {}", response.getStatusCode());
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("401 Unauthorized from appointment service: {} - Response body: {}",
                    e.getMessage(), e.getResponseBodyAsString());
            throw new RuntimeException("Appointment service returned 401: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed to get appointments from appointment service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get appointments from appointment service: " + e.getMessage(), e);
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

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<AppointmentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
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
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Appointment not found: {} - Response: {}", appointmentId, e.getResponseBodyAsString());
            throw new RuntimeException("Appointment not found: " + appointmentId, e);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Unauthorized from appointment service: {} - Response: {}",
                    e.getMessage(), e.getResponseBodyAsString());
            throw new RuntimeException("Unauthorized: " + e.getResponseBodyAsString(), e);
        } catch (HttpClientErrorException e) {
            log.error("Client error from appointment service: {} {} - Response: {}",
                    e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            throw new RuntimeException("Appointment service error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed to perform doctor action: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform doctor action: " + e.getMessage(), e);
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

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<AppointmentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    AppointmentResponse.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark appointment as completed: " + e.getMessage());
        }
    }
}
