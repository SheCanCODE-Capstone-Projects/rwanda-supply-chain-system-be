package org.example.rwandasupplychain.Controllers;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.BatchDtos.BatchRequest;
import org.example.rwandasupplychain.DTOs.BatchDtos.BatchResponse;
import org.example.rwandasupplychain.DTOs.BatchDtos.BatchUpdateRequest;
import org.example.rwandasupplychain.Services.BarcodeService;
import org.example.rwandasupplychain.Services.BatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;
    private final BarcodeService barcodeService;

    public BatchController(BatchService batchService, BarcodeService barcodeService) {
        this.batchService = batchService;
        this.barcodeService = barcodeService;
    }

    @PostMapping
    public ResponseEntity<BatchResponse> create(@Valid @RequestBody BatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.create(request));
    }

    @GetMapping
    public List<BatchResponse> getAll() {
        return batchService.getAll();
    }

    @GetMapping("/{id}")
    public BatchResponse getById(@PathVariable UUID id) {
        return batchService.getById(id);
    }

    @GetMapping("/sku/{skuId}")
    public List<BatchResponse> getBySku(@PathVariable UUID skuId) {
        return batchService.getBySku(skuId);
    }

    @PutMapping("/{id}")
    public BatchResponse update(@PathVariable UUID id, @Valid @RequestBody BatchUpdateRequest request) {
        return batchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        batchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) {
        String payload = batchService.getQrPayload(id);
        byte[] image = barcodeService.generateQrCode(payload, 300);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }
}