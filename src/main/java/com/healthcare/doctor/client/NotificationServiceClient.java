package com.healthcare.doctor.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationServiceClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;

    public NotificationServiceClient(
            RestTemplate restTemplate,
            @Value("${services.notification.base-url:http://localhost:8085}") String notificationServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    public void sendCarePlanNotification(String patientId, String doctorId, String carePlanId, String appointmentId, String consultationNotes, String token) {
        try {
            String url = notificationServiceBaseUrl + "/api/notifications/care-plan";
            log.info("Sending care plan notification to: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("patientId", patientId);
            requestBody.put("doctorId", doctorId);
            requestBody.put("carePlanId", carePlanId);
            requestBody.put("appointmentId", appointmentId);
            requestBody.put("consultationNotes", consultationNotes);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Care plan notification sent successfully for care plan: {}", carePlanId);
            } else {
                log.warn("Care plan notification failed with status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending care plan notification for care plan {}: {}", carePlanId, e.getMessage());
            // Don't throw exception - notification failure shouldn't break care plan creation flow
        }
    }
}
