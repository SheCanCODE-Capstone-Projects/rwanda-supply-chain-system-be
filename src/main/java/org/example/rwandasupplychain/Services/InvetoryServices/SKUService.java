package org.example.rwandasupplychain.Services.InvetoryServices;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.SKUDtos.SKURequest;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.SKUDtos.SKUResponse;
import org.example.rwandasupplychain.Entities.FarmerEntities.Product;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.SKU;
import org.example.rwandasupplychain.Exceptions.DuplicateResourceException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.BatchRepository;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.SKURepository;
import org.example.rwandasupplychain.Services.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SKUService {

    private final SKURepository skuRepository;
    private final BatchRepository batchRepository;
    private final ProductService productService;

    public SKUService(SKURepository skuRepository, BatchRepository batchRepository, ProductService productService) {
        this.skuRepository = skuRepository;
        this.batchRepository = batchRepository;
        this.productService = productService;
    }

    public SKUResponse create(SKURequest request) {
        if (skuRepository.existsBySkuCode(request.skuCode())) {
            throw new DuplicateResourceException("SKU code already exists: " + request.skuCode());
        }
        Product product = productService.findEntity(request.productId());
        SKU sku = new SKU();
        sku.setProduct(product);
        applyRequest(sku, request);
        return toResponse(skuRepository.save(sku));
    }

    @Transactional(readOnly = true)
    public SKUResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SKUResponse> getAll() {
        return skuRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SKUResponse> getByProduct(UUID productId) {
        return skuRepository.findByProduct_Id(productId).stream().map(this::toResponse).toList();
    }

    public SKUResponse update(UUID id, SKURequest request) {
        SKU sku = findEntity(id);
        if (!sku.getSkuCode().equals(request.skuCode()) && skuRepository.existsBySkuCode(request.skuCode())) {
            throw new DuplicateResourceException("SKU code already exists: " + request.skuCode());
        }
        if (!sku.getProduct().getId().equals(request.productId())) {
            sku.setProduct(productService.findEntity(request.productId()));
        }
        applyRequest(sku, request);
        return toResponse(skuRepository.save(sku));
    }

    public void delete(UUID id) {
        skuRepository.delete(findEntity(id));
    }

    public SKU findEntity(UUID id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + id));
    }

    private void applyRequest(SKU sku, SKURequest request) {
        sku.setSkuCode(request.skuCode());
        sku.setPrice(request.price());
        sku.setUnit(request.unit());
        if (request.lowStockThreshold() != null) {
            sku.setLowStockThreshold(request.lowStockThreshold());
        }
    }

    private SKUResponse toResponse(SKU sku) {
        Integer currentStock = batchRepository.sumActiveQuantityBySkuId(sku.getId());
        return new SKUResponse(
                sku.getId(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getSkuCode(),
                sku.getPrice(),
                sku.getUnit(),
                sku.getBarcode(),
                sku.getLowStockThreshold(),
                currentStock == null ? 0 : currentStock,
                sku.isActive(),
                sku.getCreatedAt()
        );
    }
}