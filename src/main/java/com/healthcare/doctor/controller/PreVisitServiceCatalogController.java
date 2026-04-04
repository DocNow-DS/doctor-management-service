package com.healthcare.doctor.controller;

import com.healthcare.doctor.model.PreVisitServiceCatalog;
import com.healthcare.doctor.service.PreVisitServiceCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pre-visit-services")
@CrossOrigin(origins = "*")
public class PreVisitServiceCatalogController {

    private final PreVisitServiceCatalogService serviceCatalogService;

    public PreVisitServiceCatalogController(PreVisitServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PreVisitServiceCatalog> createService(
            @RequestBody PreVisitServiceCatalog service,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String createdBy = userDetails != null ? userDetails.getUsername() : "unknown";
            PreVisitServiceCatalog created = serviceCatalogService.createService(service, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PreVisitServiceCatalog>> getActiveServices() {
        return ResponseEntity.ok(serviceCatalogService.listActiveServices());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateService(@PathVariable String id, @RequestBody PreVisitServiceCatalog service) {
        try {
            PreVisitServiceCatalog updated = serviceCatalogService.updateService(id, service);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateServiceCompat(@PathVariable String id, @RequestBody PreVisitServiceCatalog service) {
        return updateService(id, service);
    }
}
