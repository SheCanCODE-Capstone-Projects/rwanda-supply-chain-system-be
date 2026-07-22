package org.example.rwandasupplychain.Controllers.FarmerEndpoint;

import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseDtos.WarehouseResponse;
import org.example.rwandasupplychain.Services.FarmerService.WarehouseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/farmer/{farmerId}/warehouses")
public class FarmerWarehouseController {

    private final WarehouseService warehouseService;

    public FarmerWarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }


    @GetMapping
    public List<WarehouseResponse> viewWarehouses(@PathVariable UUID farmerId) {
        return warehouseService.getAllActive();
    }


    @GetMapping("/{warehouseId}")
    public WarehouseResponse viewWarehouse(@PathVariable UUID farmerId, @PathVariable UUID warehouseId) {
        return warehouseService.getById(warehouseId);
    }
}