package com.healthcare.doctor.service;

import com.healthcare.doctor.model.PreVisitServiceCatalog;
import com.healthcare.doctor.repository.PreVisitServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreVisitServiceCatalogService {

    private final PreVisitServiceCatalogRepository serviceCatalogRepository;

    public PreVisitServiceCatalog createService(PreVisitServiceCatalog service, String createdBy) {
        String normalizedName = service.getServiceName() == null ? "" : service.getServiceName().trim();
        if (normalizedName.isBlank()) {
            throw new RuntimeException("Service name is required");
        }
        if (serviceCatalogRepository.existsByServiceNameIgnoreCase(normalizedName)) {
            throw new RuntimeException("Service already exists");
        }
        if (service.getPrice() == null) {
            throw new RuntimeException("Service price is required");
        }
        if (service.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Service price must be 0 or greater");
        }

        LocalDateTime now = LocalDateTime.now();
        service.setServiceName(normalizedName);
        service.setPrice(service.getPrice().stripTrailingZeros());
        service.setNotes(service.getNotes() == null ? null : service.getNotes().trim());
        service.setActive(service.getActive() == null ? Boolean.TRUE : service.getActive());
        service.setCreatedBy(createdBy);
        service.setCreatedAt(now);
        service.setUpdatedAt(now);

        return serviceCatalogRepository.save(service);
    }

    public List<PreVisitServiceCatalog> listActiveServices() {
        return serviceCatalogRepository.findByActiveTrue()
                .stream()
                .sorted(Comparator.comparing(s -> String.valueOf(s.getServiceName()).toLowerCase()))
                .toList();
    }

    public PreVisitServiceCatalog updateService(String id, PreVisitServiceCatalog updates) {
        PreVisitServiceCatalog existing = serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        String requestedName = updates.getServiceName();
        String normalizedName = requestedName == null ? existing.getServiceName() : requestedName.trim();
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new RuntimeException("Service name is required");
        }

        if (serviceCatalogRepository.existsByServiceNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new RuntimeException("Another service with this name already exists");
        }

        existing.setServiceName(normalizedName);
        if (updates.getPrice() != null) {
            if (updates.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Service price must be 0 or greater");
            }
            existing.setPrice(updates.getPrice().stripTrailingZeros());
        }
        if (updates.getNotes() != null) {
            existing.setNotes(updates.getNotes().trim());
        }
        if (updates.getActive() != null) {
            existing.setActive(updates.getActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return serviceCatalogRepository.save(existing);
    }
}
