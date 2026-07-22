package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Entities.TransportStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransportRequestDtos {

    // Used when the farmer requests new transport
    public record TransportRequestCreate(
            @NotNull UUID productId,
            @NotNull @Positive Integer quantity,
            @NotBlank String pickupLocation,
            @NotBlank String dropoffLocation,
            @NotNull @Future LocalDate preferredDate,
            String notes
    ) {}

    // Used when the farmer edits a request that hasn't been picked up yet
    public record TransportRequestUpdate(
            @NotNull @Positive Integer quantity,
            @NotBlank String pickupLocation,
            @NotBlank String dropoffLocation,
            @NotNull @Future LocalDate preferredDate,
            String notes
    ) {}

    public record TransportRequestResponse(
            UUID id,
            UUID farmerId,
            UUID productId,
            String productName,
            Integer quantity,
            String pickupLocation,
            String dropoffLocation,
            LocalDate preferredDate,
            String notes,
            TransportStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}