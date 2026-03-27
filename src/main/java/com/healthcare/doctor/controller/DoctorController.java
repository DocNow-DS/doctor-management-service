package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.dto.DoctorProfileDto;
import com.healthcare.doctor.service.PatientServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final PatientServiceClient patientServiceClient;
    private final RestTemplate restTemplate;

    @Value("${patient.service.url:http://localhost:8081}")
    private String patientServiceUrl;

    /**
     * Get the currently authenticated doctor's profile.
     * Proxies to patient-service to get user data.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal AuthResponse.User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        // Proxy to patient service to get full user profile
        try {
            ResponseEntity<DoctorProfileDto> response = restTemplate.getForEntity(
                    patientServiceUrl + "/api/auth/users/" + currentUser.getId(),
                    DoctorProfileDto.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update the currently authenticated doctor's profile.
     * Proxies to patient-service to update user data.
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal AuthResponse.User currentUser,
            @RequestBody Map<String, Object> doctorDetails) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            restTemplate.put(patientServiceUrl + "/api/auth/users/" + currentUser.getId(), doctorDetails);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileDto> getDoctorById(@PathVariable String id) {
        try {
            ResponseEntity<DoctorProfileDto> response = restTemplate.getForEntity(
                    patientServiceUrl + "/api/auth/users/" + id,
                    DoctorProfileDto.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List> getAllDoctors() {
        // Get all users with DOCTOR role from patient service
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    patientServiceUrl + "/api/admin/doctors",
                    List.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List> getDoctorsBySpecialization(@PathVariable String specialization) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    patientServiceUrl + "/api/admin/doctors/specialty?specialty=" + specialization,
                    List.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
