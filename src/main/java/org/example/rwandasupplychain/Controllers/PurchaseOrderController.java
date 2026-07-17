package org.example.rwandasupplychain.Controllers;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.PurchaseOrderDtos.PurchaseOrderResponse;
import org.example.rwandasupplychain.DTOs.PurchaseOrderDtos.PurchaseOrderStatusUpdateRequest;
import org.example.rwandasupplychain.Services.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public List<PurchaseOrderResponse> getAll(@RequestParam(required = false) UUID buyerId,
                                               @RequestParam(required = false) UUID supplierId) {
        return purchaseOrderService.getAll(buyerId, supplierId);
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse getById(@PathVariable UUID id) {
        return purchaseOrderService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public PurchaseOrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody PurchaseOrderStatusUpdateRequest request) {
        return purchaseOrderService.updateStatus(id, request.status());
    }
}
