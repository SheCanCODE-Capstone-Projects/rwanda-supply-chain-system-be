package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Entities.BatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BatchDtos {

    public record BatchRequest(
            @NotNull UUID skuId,
            @NotBlank String batchNo,
            @NotNull @Positive Integer quantity,
            @NotNull LocalDate manufacturingDate,
            @NotNull @Future LocalDate expiryDate
    ) {}

    public record BatchUpdateRequest(
            @Positive Integer quantity,
            LocalDate expiryDate,
            BatchStatus status
    ) {}

    public record BatchResponse(
            UUID id,
            UUID skuId,
            String skuCode,
            String productName,
            String batchNo,
            Integer quantity,
            LocalDate manufacturingDate,
            LocalDate expiryDate,
            BatchStatus status,
            LocalDateTime createdAt
    ) {}
}