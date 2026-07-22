package org.example.rwandasupplychain.DTOs.FarmerDtos;

import java.util.UUID;

public class WarehouseDtos {

    public record WarehouseResponse(
            UUID id,
            String name,
            String location,
            Integer capacity,
            Integer availableCapacity,
            String contactPhone,
            boolean active
    ) {}
}