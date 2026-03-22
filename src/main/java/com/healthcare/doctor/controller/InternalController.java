package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.service.PatientServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InternalController {
    
    private final PatientServiceClient patientServiceClient;
    
    @GetMapping("/validate-patient/{patientId}")
    public ResponseEntity<?> validatePatient(@PathVariable String patientId) {
        boolean isValid = patientServiceClient.isPatientValid(patientId);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
