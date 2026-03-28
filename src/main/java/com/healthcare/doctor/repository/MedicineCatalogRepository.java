package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.MedicineCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineCatalogRepository extends MongoRepository<MedicineCatalog, String> {

    boolean existsByNameIgnoreCase(String name);

    List<MedicineCatalog> findByActiveTrue();
}
