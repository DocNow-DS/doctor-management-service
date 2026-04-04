package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.MedicineCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineCatalogRepository extends MongoRepository<MedicineCatalog, String> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    List<MedicineCatalog> findByActiveTrue();

    Optional<MedicineCatalog> findByNameIgnoreCaseAndActiveTrue(String name);
}
