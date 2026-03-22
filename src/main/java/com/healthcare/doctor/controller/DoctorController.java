package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.model.Doctor;
import com.healthcare.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Register a new doctor profile (called after auth registration in
     * patient-service).
     * Requires DOCTOR or ADMIN role.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Doctor> registerDoctor(@RequestBody Doctor doctor,
            @AuthenticationPrincipal AuthResponse.User currentUser) {
        // Link the doctor profile to the authenticated user's ID from the shared users
        // collection
        if (currentUser != null && doctor.getUserId() == null) {
            doctor.setUserId(currentUser.getId());
        }
        Doctor registeredDoctor = doctorService.registerDoctor(doctor);
        return ResponseEntity.ok(registeredDoctor);
    }

    /**
     * Get the currently authenticated doctor's profile.
     * Requires DOCTOR role.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Doctor> getMyProfile(@AuthenticationPrincipal AuthResponse.User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        // Look up doctor profile by the userId stored in the shared users collection
        return doctorService.getDoctorByUserId(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the currently authenticated doctor's profile.
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Doctor> updateMyProfile(@AuthenticationPrincipal AuthResponse.User currentUser,
            @RequestBody Doctor doctorDetails) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Doctor updated = doctorService.updateDoctorByUserId(currentUser.getId(), doctorDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Doctor> getDoctorByEmail(@PathVariable String email) {
        return doctorService.getDoctorByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor doctorDetails) {
        try {
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDetails);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> verifyDoctor(@PathVariable String id) {
        try {
            Doctor verifiedDoctor = doctorService.verifyDoctor(id);
            return ResponseEntity.ok(verifiedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> toggleDoctorStatus(@PathVariable String id) {
        try {
            Doctor updatedDoctor = doctorService.toggleDoctorStatus(id);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
