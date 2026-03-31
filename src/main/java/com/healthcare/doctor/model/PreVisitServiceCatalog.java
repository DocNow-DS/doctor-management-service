package com.healthcare.doctor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "pre_visit_service_catalog")
public class PreVisitServiceCatalog {

    @Id
    private String id;

    private String serviceName;
    private BigDecimal price;
    private String notes;
    private Boolean active;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
