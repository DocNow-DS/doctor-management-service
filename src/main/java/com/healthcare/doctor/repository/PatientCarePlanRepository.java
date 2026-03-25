package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.PatientCarePlan;
import com.healthcare.doctor.model.PatientCarePlan.CarePlanStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for PatientCarePlan documents.
 *
 * Spring Data automatically generates query implementations from method names.
 * No manual query writing needed.
 */
@Repository
public interface PatientCarePlanRepository extends MongoRepository<PatientCarePlan, String> {

    // ── Find all plans created by a specific doctor ───────────────────────────
    List<PatientCarePlan> findByDoctorId(String doctorId);

    // ── Find all plans belonging to a specific patient ────────────────────────
    List<PatientCarePlan> findByPatientId(String patientId);

    // ── Find all plans for a specific patient by a specific doctor ────────────
    List<PatientCarePlan> findByDoctorIdAndPatientId(String doctorId, String patientId);

    // ── Find a single plan by its ID, but only if it belongs to that doctor ──
    // (ownership check — doctor can't access another doctor's plans)
    Optional<PatientCarePlan> findByIdAndDoctorId(String id, String doctorId);

    // ── Find plans by doctor filtered by status (e.g. only ACTIVE plans) ─────
    List<PatientCarePlan> findByDoctorIdAndStatus(String doctorId, CarePlanStatus status);

    // ── Find plans by patient filtered by status ──────────────────────────────
    List<PatientCarePlan> findByPatientIdAndStatus(String patientId, CarePlanStatus status);

    // ── Check if a plan exists for this doctor (used for validation) ──────────
    boolean existsByIdAndDoctorId(String id, String doctorId);
}
