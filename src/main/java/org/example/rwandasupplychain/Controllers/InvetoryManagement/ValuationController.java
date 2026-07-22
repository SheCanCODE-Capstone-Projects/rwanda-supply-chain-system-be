package org.example.rwandasupplychain.Controllers.InvetoryManagement;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.FifoCogsRequest;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.FifoCogsResult;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.InventoryValuationSummary;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.ProductValuation;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.SkuValuation;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.ValuationMethod;
import org.example.rwandasupplychain.Services.InvetoryServices.InventoryValuationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/valuation")
public class ValuationController {

    private final InventoryValuationService valuationService;

    public ValuationController(InventoryValuationService valuationService) {
        this.valuationService = valuationService;
    }

    @GetMapping("/sku/{skuId}")
    public SkuValuation valueSku(@PathVariable UUID skuId,
                                 @RequestParam(defaultValue = "FIFO") ValuationMethod method) {
        return valuationService.valueSku(skuId, method);
    }

    @GetMapping("/product/{productId}")
    public ProductValuation valueProduct(@PathVariable UUID productId,
                                         @RequestParam(defaultValue = "FIFO") ValuationMethod method) {
        return valuationService.valueProduct(productId, method);
    }

    @GetMapping("/summary")
    public InventoryValuationSummary valueAll(@RequestParam(defaultValue = "FIFO") ValuationMethod method) {
        return valuationService.valueAll(method);
    }

    @GetMapping("/sku/{skuId}/fifo-cost")
    public FifoCogsResult previewFifoCost(@PathVariable UUID skuId,
                                          @RequestParam Integer quantity) {
        return valuationService.simulateFifoCogs(skuId, quantity);
    }

    @PostMapping("/fifo-cost")
    public FifoCogsResult previewFifoCostStrict(@Valid @RequestBody FifoCogsRequest request) {
        return valuationService.simulateFifoCogsStrict(request.skuId(), request.quantity());
    }
}