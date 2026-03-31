package com.healthcare.doctor.repository;

import com.healthcare.doctor.model.PreVisitServiceCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreVisitServiceCatalogRepository extends MongoRepository<PreVisitServiceCatalog, String> {

    boolean existsByServiceNameIgnoreCase(String serviceName);

    boolean existsByServiceNameIgnoreCaseAndIdNot(String serviceName, String id);

    List<PreVisitServiceCatalog> findByActiveTrue();

    Optional<PreVisitServiceCatalog> findByServiceNameIgnoreCaseAndActiveTrue(String serviceName);
}
