package org.example.rwandasupplychain.Services.FarmerService;

import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseDtos.WarehouseResponse;
import org.example.rwandasupplychain.Entities.FarmerEntities.Warehouse;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.FarmerRepositories.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    // Farmers can only browse active warehouses - not create, edit, or delete them.
    public List<WarehouseResponse> getAllActive() {
        return warehouseRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public WarehouseResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    public Warehouse findEntity(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return new WarehouseResponse(
                w.getId(),
                w.getName(),
                w.getLocation(),
                w.getCapacity(),
                w.getAvailableCapacity(),
                w.getContactPhone(),
                w.isActive()
        );
    }
}