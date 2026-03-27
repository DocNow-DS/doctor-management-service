package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

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
}
