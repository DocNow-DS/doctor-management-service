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
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (doctorRepository.existsByLicenseNumber(doctor.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }
        
        // Password is handled by patient service, store as-is for reference
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
    
    public Optional<Doctor> getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }
    
    public Doctor updateDoctor(String id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setPhoneNumber(doctorDetails.getPhoneNumber());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setHospitalName(doctorDetails.getHospitalName());
        doctor.setYearsOfExperience(doctorDetails.getYearsOfExperience());
        doctor.setEducation(doctorDetails.getEducation());
        doctor.setAbout(doctorDetails.getAbout());
        doctor.setProfileImageUrl(doctorDetails.getProfileImageUrl());
        doctor.setUpdatedAt(LocalDateTime.now());
        
        return doctorRepository.save(doctor);
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
