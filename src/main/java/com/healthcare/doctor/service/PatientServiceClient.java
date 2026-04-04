package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PatientServiceClient {
    
    private final RestTemplate restTemplate;
    private final String patientServiceUrl;
    
    public PatientServiceClient(RestTemplate restTemplate, 
                               @Value("${patient.service.url:http://localhost:8081}") String patientServiceUrl) {
        this.restTemplate = restTemplate;
        this.patientServiceUrl = patientServiceUrl;
    }
    
    public AuthResponse validateToken(String token) {
        String url = patientServiceUrl + "/api/auth/validate";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<AuthResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, AuthResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate token with patient service", e);
        }
    }
    
    public boolean isPatientValid(String patientId) {
        String url = patientServiceUrl + "/api/patients/" + patientId;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isUserValid(String userId) {
        String url = patientServiceUrl + "/api/auth/users/" + userId;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resolve patient identifier aliases (username/email/userId) into canonical patient id.
     * Returns the original identifier when no match can be found.
     */
    @SuppressWarnings("unchecked")
    public String resolveCanonicalPatientId(String identifier) {
        String normalized = normalize(identifier);
        if (normalized.isBlank()) {
            return "";
        }

        try {
            String url = patientServiceUrl + "/api/patient/all";
            List<Map<String, Object>> patients = (List<Map<String, Object>>) (List<?>) restTemplate.getForObject(url, List.class);
            if (patients == null) {
                patients = Collections.emptyList();
            }

            for (Map<String, Object> patient : patients) {
                if (patient == null) continue;

                List<String> aliases = List.of(
                        normalize(patient.get("_id")),
                        normalize(patient.get("id")),
                        normalize(patient.get("userId")),
                        normalize(patient.get("username")),
                        normalize(patient.get("email"))
                );

                boolean matched = aliases.stream()
                        .filter(value -> !value.isBlank())
                        .map(String::toLowerCase)
                        .anyMatch(value -> value.equals(normalized.toLowerCase()));

                if (matched) {
                    String canonical = firstNonBlank(
                            normalize(patient.get("_id")),
                            normalize(patient.get("id")),
                            normalize(patient.get("userId"))
                    );
                    return canonical.isBlank() ? normalized : canonical;
                }
            }
        } catch (Exception ignored) {
            // Keep original identifier if patient service lookup is unavailable.
        }

        return normalized;
    }

    private String normalize(Object value) {
        return Objects.toString(value, "").trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
