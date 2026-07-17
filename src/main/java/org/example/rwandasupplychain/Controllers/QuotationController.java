package org.example.rwandasupplychain.Controllers;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationRequest;
import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationResponse;
import org.example.rwandasupplychain.Services.QuotationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @PostMapping
    public ResponseEntity<QuotationResponse> submit(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quotationService.submit(request));
    }

    @GetMapping("/{id}")
    public QuotationResponse getById(@PathVariable UUID id) {
        return quotationService.getById(id);
    }

    @GetMapping("/rfq/{rfqId}")
    public List<QuotationResponse> getByRfq(@PathVariable UUID rfqId) {
        return quotationService.getByRfq(rfqId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<QuotationResponse> getBySupplier(@PathVariable UUID supplierId) {
        return quotationService.getBySupplier(supplierId);
    }

    @PatchMapping("/{id}/accept")
    public QuotationResponse accept(@PathVariable UUID id, @RequestParam UUID requesterId) {
        return quotationService.accept(id, requesterId);
    }

    @PatchMapping("/{id}/reject")
    public QuotationResponse reject(@PathVariable UUID id, @RequestParam UUID requesterId) {
        return quotationService.reject(id, requesterId);
    }

    @PatchMapping("/{id}/withdraw")
    public QuotationResponse withdraw(@PathVariable UUID id, @RequestParam UUID requesterId) {
        return quotationService.withdraw(id, requesterId);
    }
}
