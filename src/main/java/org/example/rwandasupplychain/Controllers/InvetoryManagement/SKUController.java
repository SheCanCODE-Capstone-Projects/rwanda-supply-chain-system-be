package org.example.rwandasupplychain.Controllers.InvetoryManagement;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.SKUDtos.SKURequest;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.SKUDtos.SKUResponse;
import org.example.rwandasupplychain.Services.InvetoryServices.BarcodeService;
import org.example.rwandasupplychain.Services.InvetoryServices.SKUService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skus")
public class SKUController {

    private final SKUService skuService;
    private final BarcodeService barcodeService;

    public SKUController(SKUService skuService, BarcodeService barcodeService) {
        this.skuService = skuService;
        this.barcodeService = barcodeService;
    }

    @PostMapping
    public ResponseEntity<SKUResponse> create(@Valid @RequestBody SKURequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skuService.create(request));
    }

    @GetMapping
    public List<SKUResponse> getAll() {
        return skuService.getAll();
    }

    @GetMapping("/{id}")
    public SKUResponse getById(@PathVariable UUID id) {
        return skuService.getById(id);
    }

    @GetMapping("/product/{productId}")
    public List<SKUResponse> getByProduct(@PathVariable UUID productId) {
        return skuService.getByProduct(productId);
    }

    @PutMapping("/{id}")
    public SKUResponse update(@PathVariable UUID id, @Valid @RequestBody SKURequest request) {
        return skuService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        skuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcode(@PathVariable UUID id) {
        SKUResponse sku = skuService.getById(id);
        byte[] image = barcodeService.generateBarcode(sku.barcode(), 300, 120);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }
}