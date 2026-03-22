package com.healthcare.doctor.service;

import com.healthcare.doctor.model.Doctor;
import com.healthcare.doctor.model.Availability;
import com.healthcare.doctor.repository.DoctorRepository;
import com.healthcare.doctor.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AvailabilityRepository availabilityRepository;

    public Doctor registerDoctor(Doctor doctor) {
        if (doctor.getEmail() != null && doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (doctor.getLicenseNumber() != null && doctorRepository.existsByLicenseNumber(doctor.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }

        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        doctor.setIsActive(true);
        doctor.setIsVerified(false);

        try {
            return doctorRepository.save(doctor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register doctor: " + e.getMessage(), e);
        }
    }

    public Optional<Doctor> getDoctorById(String id) {
        return doctorRepository.findById(id);
    }

    /** Find doctor profile by the userId stored in the shared users collection. */
    public Optional<Doctor> getDoctorByUserId(String userId) {
        return doctorRepository.findByUserId(userId);
    }

    public Optional<Doctor> getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public Doctor updateDoctor(String id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        applyUpdates(doctor, doctorDetails);
        return doctorRepository.save(doctor);
    }

    /** Update doctor profile looked up by userId (shared users collection ID). */
    public Doctor updateDoctorByUserId(String userId, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for this user"));
        applyUpdates(doctor, doctorDetails);
        return doctorRepository.save(doctor);
    }

    private void applyUpdates(Doctor doctor, Doctor doctorDetails) {
        if (doctorDetails.getFirstName() != null)
            doctor.setFirstName(doctorDetails.getFirstName());
        if (doctorDetails.getLastName() != null)
            doctor.setLastName(doctorDetails.getLastName());
        if (doctorDetails.getPhoneNumber() != null)
            doctor.setPhoneNumber(doctorDetails.getPhoneNumber());
        if (doctorDetails.getSpecialization() != null)
            doctor.setSpecialization(doctorDetails.getSpecialization());
        if (doctorDetails.getHospitalName() != null)
            doctor.setHospitalName(doctorDetails.getHospitalName());
        if (doctorDetails.getYearsOfExperience() != null)
            doctor.setYearsOfExperience(doctorDetails.getYearsOfExperience());
        if (doctorDetails.getEducation() != null)
            doctor.setEducation(doctorDetails.getEducation());
        if (doctorDetails.getAbout() != null)
            doctor.setAbout(doctorDetails.getAbout());
        if (doctorDetails.getProfileImageUrl() != null)
            doctor.setProfileImageUrl(doctorDetails.getProfileImageUrl());
        doctor.setUpdatedAt(LocalDateTime.now());
    }

    public void deleteDoctor(String id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Doctor not found");
        }
        doctorRepository.deleteById(id);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findAll().stream()
                .filter(doctor -> specialization.equals(doctor.getSpecialization()))
                .toList();
    }

    public Availability setAvailability(String doctorId, Availability availability) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found");
        }

        availability.setDoctorId(doctorId);
        availability.setIsActive(true);

        Optional<Availability> existingAvailability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, availability.getDayOfWeek());

        if (existingAvailability.isPresent()) {
            availability.setId(existingAvailability.get().getId());
        }

        return availabilityRepository.save(availability);
    }

    public List<Availability> getDoctorAvailability(String doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found");
        }
        return availabilityRepository.findByDoctorId(doctorId);
    }

    public void removeAvailability(String doctorId, String availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!doctorId.equals(availability.getDoctorId())) {
            throw new RuntimeException("Access denied");
        }

        availabilityRepository.deleteById(availabilityId);
    }

    public Doctor verifyDoctor(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setIsVerified(true);
        doctor.setUpdatedAt(LocalDateTime.now());

        return doctorRepository.save(doctor);
    }

    public Doctor toggleDoctorStatus(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setIsActive(!doctor.getIsActive());
        doctor.setUpdatedAt(LocalDateTime.now());

        return doctorRepository.save(doctor);
    }
}
