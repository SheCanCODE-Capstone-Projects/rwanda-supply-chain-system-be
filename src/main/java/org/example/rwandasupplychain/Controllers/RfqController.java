package org.example.rwandasupplychain.Controllers;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.RfqDtos.RfqRequest;
import org.example.rwandasupplychain.DTOs.RfqDtos.RfqResponse;
import org.example.rwandasupplychain.DTOs.SupplierMatchDtos.SupplierMatch;
import org.example.rwandasupplychain.Enums.RfqStatus;
import org.example.rwandasupplychain.Services.RfqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rfqs")
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    @PostMapping
    public ResponseEntity<RfqResponse> create(@Valid @RequestBody RfqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rfqService.create(request));
    }

    @GetMapping
    public List<RfqResponse> getAll(@RequestParam(required = false) UUID buyerId,
                                     @RequestParam(required = false) RfqStatus status,
                                     @RequestParam(required = false) String category) {
        return rfqService.getAll(buyerId, status, category);
    }

    @GetMapping("/{id}")
    public RfqResponse getById(@PathVariable UUID id) {
        return rfqService.getById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<SupplierMatch> getRecommendations(@PathVariable UUID id,
                                                   @RequestParam(defaultValue = "10") int limit) {
        return rfqService.getRecommendations(id, limit);
    }

    @GetMapping("/matches")
    public List<RfqResponse> getMatchesForSupplier(@RequestParam UUID supplierId) {
        return rfqService.getMatchesForSupplier(supplierId);
    }

    @PatchMapping("/{id}/cancel")
    public RfqResponse cancel(@PathVariable UUID id, @RequestParam UUID requesterId) {
        return rfqService.cancel(id, requesterId);
    }
}
