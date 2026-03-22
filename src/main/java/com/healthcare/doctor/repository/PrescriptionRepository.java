package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByDoctorId(String doctorId);
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByDoctorIdAndPatientId(String doctorId, String patientId);
    Optional<Prescription> findByIdAndDoctorId(String id, String doctorId);
    List<Prescription> findByDoctorIdAndIssuedDateBetween(String doctorId, LocalDateTime startDate, LocalDateTime endDate);
    void deleteByDoctorIdAndId(String doctorId, String id);
}
