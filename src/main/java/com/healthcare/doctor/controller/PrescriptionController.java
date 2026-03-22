package com.healthcare.doctor.controller;

import com.healthcare.doctor.model.Prescription;
import com.healthcare.doctor.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Prescription> issuePrescription(@RequestBody Prescription prescription) {
        try {
            Prescription issuedPrescription = prescriptionService.issuePrescription(prescription);
            return ResponseEntity.ok(issuedPrescription);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable String id) {
        return prescriptionService.getPrescriptionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDoctor(@PathVariable String doctorId) {
        try {
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId);
            return ResponseEntity.ok(prescriptions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByPatient(@PathVariable String patientId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
        return ResponseEntity.ok(prescriptions);
    }
    
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDoctorAndPatient(
            @PathVariable String doctorId, @PathVariable String patientId) {
        try {
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorAndPatient(doctorId, patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<Prescription> updatePrescription(
            @PathVariable String id, @PathVariable String doctorId, @RequestBody Prescription prescriptionDetails) {
        try {
            Prescription updatedPrescription = prescriptionService.updatePrescription(id, doctorId, prescriptionDetails);
            return ResponseEntity.ok(updatedPrescription);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/deactivate/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<Void> deactivatePrescription(@PathVariable String id, @PathVariable String doctorId) {
        try {
            prescriptionService.deactivatePrescription(id, doctorId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id")
    public ResponseEntity<Void> deletePrescription(@PathVariable String id, @PathVariable String doctorId) {
        try {
            prescriptionService.deletePrescription(id, doctorId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/doctor/{doctorId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDateRange(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDateRange(doctorId, startDate, endDate);
            return ResponseEntity.ok(prescriptions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
