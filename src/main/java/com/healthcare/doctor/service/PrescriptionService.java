package com.healthcare.doctor.service;

import com.healthcare.doctor.model.Prescription;
import com.healthcare.doctor.repository.PrescriptionRepository;
import com.healthcare.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PatientServiceClient patientServiceClient;
    
    public Prescription issuePrescription(Prescription prescription) {
        if (!doctorRepository.existsById(prescription.getDoctorId())) {
            throw new RuntimeException("Doctor not found");
        }
        
        // Validate patient exists in patient service
        if (!patientServiceClient.isPatientValid(prescription.getPatientId())) {
            throw new RuntimeException("Patient not found");
        }
        
        prescription.setIssuedDate(LocalDateTime.now());
        prescription.setExpiryDate(LocalDateTime.now().plusDays(prescription.getDurationDays()));
        prescription.setIsActive(true);
        
        return prescriptionRepository.save(prescription);
    }
    
    public Optional<Prescription> getPrescriptionById(String id) {
        return prescriptionRepository.findById(id);
    }
    
    public Optional<Prescription> getPrescriptionByIdAndDoctor(String id, String doctorId) {
        return prescriptionRepository.findByIdAndDoctorId(id, doctorId);
    }
    
    public List<Prescription> getPrescriptionsByDoctor(String doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found");
        }
        return prescriptionRepository.findByDoctorId(doctorId);
    }
    
    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        // Validate patient exists
        if (!patientServiceClient.isPatientValid(patientId)) {
            throw new RuntimeException("Patient not found");
        }
        return prescriptionRepository.findByPatientId(patientId);
    }
    
    public List<Prescription> getPrescriptionsByDoctorAndPatient(String doctorId, String patientId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found");
        }
        
        // Validate patient exists
        if (!patientServiceClient.isPatientValid(patientId)) {
            throw new RuntimeException("Patient not found");
        }
        
        return prescriptionRepository.findByDoctorIdAndPatientId(doctorId, patientId);
    }
    
    public Prescription updatePrescription(String id, String doctorId, Prescription prescriptionDetails) {
        Prescription prescription = prescriptionRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        // Validate patient if patient ID is being updated
        if (prescriptionDetails.getPatientId() != null && 
            !prescriptionDetails.getPatientId().equals(prescription.getPatientId())) {
            if (!patientServiceClient.isPatientValid(prescriptionDetails.getPatientId())) {
                throw new RuntimeException("Patient not found");
            }
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
    
    public void deactivatePrescription(String id, String doctorId) {
        Prescription prescription = prescriptionRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        prescription.setIsActive(false);
        prescriptionRepository.save(prescription);
    }
    
    public void deletePrescription(String id, String doctorId) {
        Prescription prescription = prescriptionRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Prescription not found or access denied"));
        
        prescriptionRepository.deleteById(id);
    }
    
    public List<Prescription> getPrescriptionsByDateRange(String doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found");
        }
        return prescriptionRepository.findByDoctorIdAndIssuedDateBetween(doctorId, startDate, endDate);
    }
}
