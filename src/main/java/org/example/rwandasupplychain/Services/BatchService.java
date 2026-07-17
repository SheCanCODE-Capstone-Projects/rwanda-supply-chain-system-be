package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.BatchDtos.BatchRequest;
import org.example.rwandasupplychain.DTOs.BatchDtos.BatchResponse;
import org.example.rwandasupplychain.DTOs.BatchDtos.BatchUpdateRequest;
import org.example.rwandasupplychain.Entities.Batch;
import org.example.rwandasupplychain.Entities.BatchStatus;
import org.example.rwandasupplychain.Entities.SKU;
import org.example.rwandasupplychain.Exceptions.DuplicateResourceException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.BatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BatchService {

    private final BatchRepository batchRepository;
    private final SKUService skuService;

    public BatchService(BatchRepository batchRepository, SKUService skuService) {
        this.batchRepository = batchRepository;
        this.skuService = skuService;
    }

    public BatchResponse create(BatchRequest request) {
        SKU sku = skuService.findEntity(request.skuId());
        batchRepository.findBySku_IdAndBatchNo(sku.getId(), request.batchNo()).ifPresent(b -> {
            throw new DuplicateResourceException("Batch number '" + request.batchNo() + "' already exists for this SKU");
        });

        Batch batch = new Batch();
        batch.setSku(sku);
        batch.setBatchNo(request.batchNo());
        batch.setQuantity(request.quantity());
        batch.setManufacturingDate(request.manufacturingDate());
        batch.setExpiryDate(request.expiryDate());
        batch.setUnitCost(request.unitCost() != null ? request.unitCost() : java.math.BigDecimal.ZERO);
        batch.setStatus(BatchStatus.ACTIVE);
        Batch saved = batchRepository.save(batch);

        saved.setQrPayload(buildQrPayload(saved));
        return toResponse(batchRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public BatchResponse getById(UUID id) {
        return toResponse(refreshExpiry(findEntity(id)));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getAll() {
        return batchRepository.findAll().stream().map(this::refreshExpiry).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBySku(UUID skuId) {
        return batchRepository.findBySku_Id(skuId).stream().map(this::refreshExpiry).map(this::toResponse).toList();
    }

    public BatchResponse update(UUID id, BatchUpdateRequest request) {
        Batch batch = findEntity(id);
        if (request.quantity() != null) {
            batch.setQuantity(request.quantity());
        }
        if (request.expiryDate() != null) {
            batch.setExpiryDate(request.expiryDate());
        }
        if (request.status() != null) {
            batch.setStatus(request.status());
        }
        if (request.unitCost() != null) {
            batch.setUnitCost(request.unitCost());
        }
        return toResponse(batchRepository.save(batch));
    }

    public void delete(UUID id) {
        batchRepository.delete(findEntity(id));
    }

    public Batch findEntity(UUID id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + id));
    }

    @Transactional(readOnly = true)
    public String getQrPayload(UUID id) {
        Batch batch = findEntity(id);
        return batch.getQrPayload() != null ? batch.getQrPayload() : buildQrPayload(batch);
    }

    private Batch refreshExpiry(Batch batch) {
        if (batch.getStatus() == BatchStatus.ACTIVE && !batch.getExpiryDate().isAfter(LocalDate.now())) {
            batch.setStatus(BatchStatus.EXPIRED);
            batchRepository.save(batch);
        }
        return batch;
    }

    private String buildQrPayload(Batch batch) {
        return String.format(
                "{\"batchId\":\"%s\",\"batchNo\":\"%s\",\"skuCode\":\"%s\",\"expiryDate\":\"%s\"}",
                batch.getId(), batch.getBatchNo(), batch.getSku().getSkuCode(), batch.getExpiryDate()
        );
    }

    private BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getSku().getId(),
                batch.getSku().getSkuCode(),
                batch.getSku().getProduct().getName(),
                batch.getBatchNo(),
                batch.getQuantity(),
                batch.getManufacturingDate(),
                batch.getExpiryDate(),
                batch.getStatus(),
                batch.getUnitCost(),
                batch.getCreatedAt()
        );
    }
}