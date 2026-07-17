package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.rwandasupplychain.Entities.ProducerType;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDtos {

    public record ProductRequest(
            @NotNull UUID orgId,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String unit,
            String description,
            @NotNull ProducerType producerType
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
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}