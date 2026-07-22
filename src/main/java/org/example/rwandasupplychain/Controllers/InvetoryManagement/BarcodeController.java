package org.example.rwandasupplychain.Controllers.InvetoryManagement;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.BarcodeDtos.DecodeResponse;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.BatchDtos.BatchResponse;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.SKUDtos.SKUResponse;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.SKU;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.SKURepository;
import org.example.rwandasupplychain.Services.InvetoryServices.BarcodeService;
import org.example.rwandasupplychain.Services.InvetoryServices.BatchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/barcode")
public class BarcodeController {

    private static final Pattern BATCH_ID_PATTERN = Pattern.compile("\"batchId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private final BarcodeService barcodeService;
    private final BatchService batchService;
    private final SKURepository skuRepository;

    public BarcodeController(BarcodeService barcodeService, BatchService batchService, SKURepository skuRepository) {
        this.barcodeService = barcodeService;
        this.batchService = batchService;
        this.skuRepository = skuRepository;
    }

    @PostMapping(value = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DecodeResponse decode(@RequestParam MultipartFile file) {
        String content = barcodeService.decode(file);

        Matcher matcher = BATCH_ID_PATTERN.matcher(content);
        if (matcher.find()) {
            try {
                BatchResponse batch = batchService.getById(UUID.fromString(matcher.group(1)));
                return new DecodeResponse(content, "BATCH", batch);
            } catch (Exception ignored) {
                // not a resolvable batch id — fall through to SKU lookup
            }
        }

        return skuRepository.findByBarcode(content)
                .map(sku -> new DecodeResponse(content, "SKU", skuToSummary(sku)))
                .orElseGet(() -> new DecodeResponse(content, "UNKNOWN", null));
    }

    private SKUResponse skuToSummary(SKU sku) {
        return new SKUResponse(
                sku.getId(), sku.getProduct().getId(), sku.getProduct().getName(), sku.getSkuCode(),
                sku.getPrice(), sku.getUnit(), sku.getBarcode(), sku.getLowStockThreshold(), null, sku.isActive(), sku.getCreatedAt()
        );
    }
}