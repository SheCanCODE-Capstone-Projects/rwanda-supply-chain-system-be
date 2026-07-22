package org.example.rwandasupplychain.Controllers.InvetoryManagement;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.InventoryAlertDtos.ExpiringBatchAlert;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.InventoryAlertDtos.LowStockAlert;
import org.example.rwandasupplychain.Services.InvetoryServices.InventoryAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/alerts")
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    public InventoryAlertController(InventoryAlertService inventoryAlertService) {
        this.inventoryAlertService = inventoryAlertService;
    }

    @GetMapping("/low-stock")
    public List<LowStockAlert> lowStock() {
        return inventoryAlertService.getLowStockAlerts();
    }

    @GetMapping("/expiring")
    public List<ExpiringBatchAlert> expiring(@RequestParam(defaultValue = "30") int days) {
        return inventoryAlertService.getExpiringBatches(days);
    }

    @GetMapping("/expired")
    public List<ExpiringBatchAlert> expired() {
        return inventoryAlertService.getExpiredBatches();
    }
}