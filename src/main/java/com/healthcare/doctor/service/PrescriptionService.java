package com.healthcare.doctor.service;

import com.healthcare.doctor.model.Prescription;
import com.healthcare.doctor.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final PatientServiceClient patientServiceClient;
    
    public Prescription issuePrescription(Prescription prescription) {
        // Validate user exists in patient service and has DOCTOR role
        if (!patientServiceClient.isUserValid(prescription.getUserId())) {
            throw new RuntimeException("Doctor not found");
        }
        
        // Validate patient exists in patient service
        // if (!patientServiceClient.isPatientValid(prescription.getPatientId())) {
        //     throw new RuntimeException("Patient not found");
        // }
        
        prescription.setIssuedDate(LocalDateTime.now());
        prescription.setExpiryDate(LocalDateTime.now().plusDays(prescription.getDurationDays()));
        prescription.setIsActive(true);
        
        return prescriptionRepository.save(prescription);
    }
    
    public Optional<Prescription> getPrescriptionById(String id) {
        return prescriptionRepository.findById(id);
    }
    
    public Optional<Prescription> getPrescriptionByIdAndUser(String id, String userId) {
        return prescriptionRepository.findByIdAndUserId(id, userId);
    }
    
    public List<Prescription> getPrescriptionsByUser(String userId) {
        return prescriptionRepository.findByUserId(userId);
    }
    
    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        // Validate patient exists
        // if (!patientServiceClient.isPatientValid(patientId)) {
        //     throw new RuntimeException("Patient not found");
        // }
        return prescriptionRepository.findByPatientId(patientId);
    }
    
    public List<Prescription> getPrescriptionsByUserAndPatient(String userId, String patientId) {
        return prescriptionRepository.findByUserIdAndPatientId(userId, patientId);
    }
    
    public Prescription updatePrescription(String id, String userId, Prescription prescriptionDetails) {
        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        // Validate patient if patient ID is being updated
        if (prescriptionDetails.getPatientId() != null && 
            !prescriptionDetails.getPatientId().equals(prescription.getPatientId())) {
            // if (!patientServiceClient.isPatientValid(prescriptionDetails.getPatientId())) {
            //     throw new RuntimeException("Patient not found");
            // }
        }
        
        prescription.setDiagnosis(prescriptionDetails.getDiagnosis());
        prescription.setMedications(prescriptionDetails.getMedications());
        prescription.setDosage(prescriptionDetails.getDosage());
        prescription.setInstructions(prescriptionDetails.getInstructions());
        prescription.setDurationDays(prescriptionDetails.getDurationDays());
        prescription.setNotes(prescriptionDetails.getNotes());
        prescription.setPrescriptionUrl(prescriptionDetails.getPrescriptionUrl());
        prescription.setExpiryDate(LocalDateTime.now().plusDays(prescriptionDetails.getDurationDays()));
        
        return prescriptionRepository.save(prescription);
    }
    
    public void deactivatePrescription(String id, String userId) {
        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        prescription.setIsActive(false);
        prescriptionRepository.save(prescription);
    }
    
    public void deletePrescription(String id, String userId) {
        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        prescriptionRepository.deleteById(id);
    }
    
    public List<Prescription> getPrescriptionsByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return prescriptionRepository.findByUserIdAndIssuedDateBetween(userId, startDate, endDate);
    }
}
