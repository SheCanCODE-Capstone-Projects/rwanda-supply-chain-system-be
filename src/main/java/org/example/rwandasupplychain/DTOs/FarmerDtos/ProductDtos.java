package org.example.rwandasupplychain.DTOs.FarmerDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.rwandasupplychain.Entities.FarmerEntities.ProducerType;
import org.example.rwandasupplychain.Entities.FarmerEntities.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDtos {

    public record ProductRequest(
            @NotNull UUID orgId,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String unit,
            String description,
            @NotNull ProducerType producerType,
            @NotNull @PositiveOrZero BigDecimal price,
            @NotNull @Positive Integer quantity,
            @NotBlank String batch,
            @NotNull ProductStatus status
    ) {}

    public record ProductResponse(
            UUID id,
            UUID orgId,
            String name,
            String category,
            String unit,
            String description,
            ProducerType producerType,
            boolean active,
            BigDecimal price,
            Integer quantity,
            String batch,
            ProductStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}