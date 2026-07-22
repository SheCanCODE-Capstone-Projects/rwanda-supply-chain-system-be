package org.example.rwandasupplychain.DTOs.FarmerDtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Entities.FarmerEntities.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseBookingDtos {

    public record WarehouseBookingCreate(
            @NotNull UUID warehouseId,
            @NotNull UUID productId,
            @NotNull @Positive Integer quantity,
            @NotNull @FutureOrPresent LocalDate startDate,
            @NotNull @Future LocalDate endDate,
            String notes
    ) {}

    // Used when the farmer edits a booking that hasn't been confirmed yet
    public record WarehouseBookingUpdate(
            @NotNull @Positive Integer quantity,
            @NotNull @FutureOrPresent LocalDate startDate,
            @NotNull @Future LocalDate endDate,
            String notes
    ) {}

    public record WarehouseBookingResponse(
            UUID id,
            UUID farmerId,
            UUID warehouseId,
            String warehouseName,
            UUID productId,
            String productName,
            Integer quantity,
            LocalDate startDate,
            LocalDate endDate,
            String notes,
            BookingStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}