package org.example.rwandasupplychain.Controllers.InvetoryManagement;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.StockMovementDtos.StockMovementRequest;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.StockMovementDtos.StockMovementResponse;
import org.example.rwandasupplychain.Services.InvetoryServices.StockMovementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> record(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMovementService.record(request));
    }

    @GetMapping("/batch/{batchId}")
    public List<StockMovementResponse> getHistory(@PathVariable UUID batchId) {
        return stockMovementService.getHistory(batchId);
    }
}