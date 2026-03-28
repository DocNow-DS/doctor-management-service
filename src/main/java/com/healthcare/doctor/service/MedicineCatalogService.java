package com.healthcare.doctor.service;

import com.healthcare.doctor.model.MedicineCatalog;
import com.healthcare.doctor.repository.MedicineCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineCatalogService {

    private final MedicineCatalogRepository medicineCatalogRepository;

    public MedicineCatalog createMedicine(MedicineCatalog medicine, String createdBy) {
        String normalizedName = medicine.getName() == null ? "" : medicine.getName().trim();
        if (normalizedName.isBlank()) {
            throw new RuntimeException("Medicine name is required");
        }
        if (medicineCatalogRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new RuntimeException("Medicine already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        medicine.setName(normalizedName);
        medicine.setGenericName(medicine.getGenericName() == null ? null : medicine.getGenericName().trim());
        medicine.setForm(medicine.getForm() == null ? null : medicine.getForm().trim());
        medicine.setStrength(medicine.getStrength() == null ? null : medicine.getStrength().trim());
        medicine.setNotes(medicine.getNotes() == null ? null : medicine.getNotes().trim());
        medicine.setActive(medicine.getActive() == null ? Boolean.TRUE : medicine.getActive());
        medicine.setCreatedBy(createdBy);
        medicine.setCreatedAt(now);
        medicine.setUpdatedAt(now);

        return medicineCatalogRepository.save(medicine);
    }

    public List<MedicineCatalog> listActiveMedicines() {
        return medicineCatalogRepository.findByActiveTrue()
                .stream()
                .sorted(Comparator.comparing(m -> String.valueOf(m.getName()).toLowerCase()))
                .toList();
    }
}
