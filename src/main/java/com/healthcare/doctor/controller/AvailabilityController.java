package com.healthcare.doctor.controller;

import com.healthcare.doctor.model.Availability;
import com.healthcare.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AvailabilityController {
    
    private final DoctorService doctorService;
    
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Availability> setAvailability(@PathVariable String doctorId, @RequestBody Availability availability) {
        try {
            Availability savedAvailability = doctorService.setAvailability(doctorId, availability);
            return ResponseEntity.ok(savedAvailability);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Availability>> getDoctorAvailability(@PathVariable String doctorId) {
        try {
            List<Availability> availability = doctorService.getDoctorAvailability(doctorId);
            return ResponseEntity.ok(availability);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/doctor/{doctorId}/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR') and #doctorId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> removeAvailability(@PathVariable String doctorId, @PathVariable String availabilityId) {
        try {
            doctorService.removeAvailability(doctorId, availabilityId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
