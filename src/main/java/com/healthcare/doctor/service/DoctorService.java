package com.healthcare.doctor.service;

import com.healthcare.doctor.model.Availability;
import com.healthcare.doctor.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final AvailabilityRepository availabilityRepository;
    private final PatientServiceClient patientServiceClient;

    public Availability setAvailability(String userId, Availability availability) {
        // Validate user exists in patient service
        if (!patientServiceClient.isUserValid(userId)) {
            throw new RuntimeException("Doctor not found");
        }

        availability.setUserId(userId);
        availability.setIsActive(true);

        Optional<Availability> existingAvailability = availabilityRepository
                .findByUserIdAndDayOfWeek(userId, availability.getDayOfWeek());

        if (existingAvailability.isPresent()) {
            availability.setId(existingAvailability.get().getId());
        }

        return availabilityRepository.save(availability);
    }

    public List<Availability> getUserAvailability(String userId) {
        // Validate user exists in patient service
        if (!patientServiceClient.isUserValid(userId)) {
            throw new RuntimeException("Doctor not found");
        }
        return availabilityRepository.findByUserId(userId);
    }

    public void removeAvailability(String userId, String availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!userId.equals(availability.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        availabilityRepository.deleteById(availabilityId);
    }
}
