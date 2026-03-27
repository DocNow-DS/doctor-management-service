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
    
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('DOCTOR') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Availability> setAvailability(@PathVariable String userId, @RequestBody Availability availability) {
        try {
            Availability savedAvailability = doctorService.setAvailability(userId, availability);
            return ResponseEntity.ok(savedAvailability);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Availability>> getUserAvailability(@PathVariable String userId) {
        try {
            List<Availability> availability = doctorService.getUserAvailability(userId);
            return ResponseEntity.ok(availability);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/user/{userId}/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> removeAvailability(@PathVariable String userId, @PathVariable String availabilityId) {
        try {
            doctorService.removeAvailability(userId, availabilityId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
