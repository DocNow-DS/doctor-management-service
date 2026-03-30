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
@Document(collection = "medicine_catalog")
public class MedicineCatalog {

    @Id
    private String id;

    private String name;
    private BigDecimal price;
    private String form;
    private String strength;
    private String notes;
    private Boolean active;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
