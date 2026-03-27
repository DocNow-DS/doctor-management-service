package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByUserId(String userId);
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByUserIdAndPatientId(String userId, String patientId);
    Optional<Prescription> findByIdAndUserId(String id, String userId);
    List<Prescription> findByUserIdAndIssuedDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    void deleteByUserIdAndId(String userId, String id);
}
