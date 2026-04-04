package com.healthcare.doctor.controller;

import com.healthcare.doctor.model.MedicineCatalog;
import com.healthcare.doctor.service.MedicineCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicineCatalogController {

    private final MedicineCatalogService medicineCatalogService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicineCatalog> createMedicine(
            @RequestBody MedicineCatalog medicine,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String createdBy = userDetails != null ? userDetails.getUsername() : "unknown";
            MedicineCatalog created = medicineCatalogService.createMedicine(medicine, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<MedicineCatalog>> getActiveMedicines() {
        return ResponseEntity.ok(medicineCatalogService.listActiveMedicines());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateMedicine(@PathVariable String id, @RequestBody MedicineCatalog medicine) {
        try {
            MedicineCatalog updated = medicineCatalogService.updateMedicine(id, medicine);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateMedicineCompat(@PathVariable String id, @RequestBody MedicineCatalog medicine) {
        return updateMedicine(id, medicine);
    }
}
