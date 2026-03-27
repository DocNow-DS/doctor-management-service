package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.Availability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByUserId(String userId);
    Optional<Availability> findByUserIdAndDayOfWeek(String userId, DayOfWeek dayOfWeek);
    void deleteByUserId(String userId);
}
