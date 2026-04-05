package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.service.PatientServiceClient;
import com.healthcare.doctor.service.PatientCarePlanService;
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
    private final PatientCarePlanService patientCarePlanService;
    
    @GetMapping("/validate-patient/{patientId}")
    public ResponseEntity<?> validatePatient(@PathVariable String patientId) {
        boolean isValid = patientServiceClient.isPatientValid(patientId);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * Internal endpoint to mark a care plan as paid/inactive.
     * Intended for trusted internal callers (payment service).
     */
    @PostMapping("/care-plans/{carePlanId}/mark-paid")
    public ResponseEntity<?> markCarePlanPaid(@PathVariable String carePlanId) {
        try {
            var plan = patientCarePlanService.completeCarePlanInternal(carePlanId);
            return ResponseEntity.ok(Map.of("id", plan.getId(), "status", plan.getStatus()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}
