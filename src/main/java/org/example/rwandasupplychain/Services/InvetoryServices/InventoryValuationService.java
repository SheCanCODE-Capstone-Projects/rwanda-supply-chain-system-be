package org.example.rwandasupplychain.Services.InvetoryServices;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.BatchLayer;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.ConsumedLayer;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.FifoCogsResult;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.InventoryValuationSummary;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.ProductValuation;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.ValuationDtos.SkuValuation;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Batch;
import org.example.rwandasupplychain.Entities.FarmerEntities.Product;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.SKU;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.ValuationMethod;
import org.example.rwandasupplychain.Exceptions.InsufficientStockException;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.BatchRepository;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.SKURepository;
import org.example.rwandasupplychain.Services.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventoryValuationService {

    private static final int MONEY_SCALE = 2;

    private final BatchRepository batchRepository;
    private final SKURepository skuRepository;
    private final SKUService skuService;
    private final ProductService productService;

    public InventoryValuationService(BatchRepository batchRepository,
                                     SKURepository skuRepository,
                                     SKUService skuService,
                                     ProductService productService) {
        this.batchRepository = batchRepository;
        this.skuRepository = skuRepository;
        this.skuService = skuService;
        this.productService = productService;
    }

    public SkuValuation valueSku(UUID skuId, ValuationMethod method) {
        SKU sku = skuService.findEntity(skuId);
        return valueSku(sku, method);
    }

    private SkuValuation valueSku(SKU sku, ValuationMethod method) {
        List<Batch> fifoLayers = batchRepository.findFifoLayersBySkuId(sku.getId());

        int totalQuantity = fifoLayers.stream().mapToInt(Batch::getQuantity).sum();

        if (method == ValuationMethod.WEIGHTED_AVERAGE) {
            return valueWeightedAverage(sku, fifoLayers, totalQuantity);
        }
        return valueFifo(sku, fifoLayers, totalQuantity);
    }

    private SkuValuation valueFifo(SKU sku, List<Batch> fifoLayers, int totalQuantity) {
        List<BatchLayer> layers = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Batch batch : fifoLayers) {
            BigDecimal layerValue = batch.getUnitCost()
                    .multiply(BigDecimal.valueOf(batch.getQuantity()))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            totalValue = totalValue.add(layerValue);
            layers.add(new BatchLayer(
                    batch.getId(),
                    batch.getBatchNo(),
                    batch.getQuantity(),
                    batch.getUnitCost(),
                    layerValue,
                    batch.getManufacturingDate(),
                    batch.getExpiryDate()
            ));
        }

        BigDecimal averageUnitCost = averageCost(totalValue, totalQuantity);

        return new SkuValuation(
                sku.getId(),
                sku.getSkuCode(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                ValuationMethod.FIFO,
                totalQuantity,
                averageUnitCost,
                totalValue,
                layers
        );
    }

    private SkuValuation valueWeightedAverage(SKU sku, List<Batch> fifoLayers, int totalQuantity) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Batch batch : fifoLayers) {
            totalCost = totalCost.add(batch.getUnitCost().multiply(BigDecimal.valueOf(batch.getQuantity())));
        }

        BigDecimal weightedAverageUnitCost = averageCost(totalCost, totalQuantity);
        BigDecimal totalValue = weightedAverageUnitCost
                .multiply(BigDecimal.valueOf(totalQuantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        List<BatchLayer> layers = new ArrayList<>();
        for (Batch batch : fifoLayers) {
            BigDecimal layerValue = weightedAverageUnitCost
                    .multiply(BigDecimal.valueOf(batch.getQuantity()))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            layers.add(new BatchLayer(
                    batch.getId(),
                    batch.getBatchNo(),
                    batch.getQuantity(),
                    weightedAverageUnitCost,
                    layerValue,
                    batch.getManufacturingDate(),
                    batch.getExpiryDate()
            ));
        }

        return new SkuValuation(
                sku.getId(),
                sku.getSkuCode(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                ValuationMethod.WEIGHTED_AVERAGE,
                totalQuantity,
                weightedAverageUnitCost,
                totalValue,
                layers
        );
    }

    public ProductValuation valueProduct(UUID productId, ValuationMethod method) {
        Product product = productService.findEntity(productId);
        List<SKU> skus = skuRepository.findByProduct_Id(productId);

        List<SkuValuation> skuValuations = skus.stream()
                .map(sku -> valueSku(sku, method))
                .toList();

        int totalQuantity = skuValuations.stream().mapToInt(SkuValuation::totalQuantity).sum();
        BigDecimal totalValue = skuValuations.stream()
                .map(SkuValuation::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductValuation(
                product.getId(),
                product.getName(),
                method,
                totalQuantity,
                totalValue,
                skuValuations
        );
    }

    public InventoryValuationSummary valueAll(ValuationMethod method) {
        List<SkuValuation> skuValuations = skuRepository.findAll().stream()
                .map(sku -> valueSku(sku, method))
                .toList();

        int totalQuantity = skuValuations.stream().mapToInt(SkuValuation::totalQuantity).sum();
        BigDecimal totalValue = skuValuations.stream()
                .map(SkuValuation::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InventoryValuationSummary(
                method,
                LocalDateTime.now(),
                skuValuations.size(),
                totalQuantity,
                totalValue,
                skuValuations
        );
    }

    public FifoCogsResult simulateFifoCogs(UUID skuId, Integer quantity) {
        SKU sku = skuService.findEntity(skuId);
        List<Batch> fifoLayers = batchRepository.findFifoLayersBySkuId(skuId);

        int available = fifoLayers.stream().mapToInt(Batch::getQuantity).sum();
        int remainingToConsume = quantity;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ConsumedLayer> consumed = new ArrayList<>();

        for (Batch batch : fifoLayers) {
            if (remainingToConsume <= 0) {
                break;
            }
            int consumeFromBatch = Math.min(remainingToConsume, batch.getQuantity());
            BigDecimal cost = batch.getUnitCost()
                    .multiply(BigDecimal.valueOf(consumeFromBatch))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            consumed.add(new ConsumedLayer(
                    batch.getId(),
                    batch.getBatchNo(),
                    consumeFromBatch,
                    batch.getUnitCost(),
                    cost
            ));

            totalCost = totalCost.add(cost);
            remainingToConsume -= consumeFromBatch;
        }

        int quantityCovered = quantity - Math.max(remainingToConsume, 0);
        BigDecimal averageUnitCost = averageCost(totalCost, quantityCovered);

        return new FifoCogsResult(
                sku.getId(),
                sku.getSkuCode(),
                quantity,
                available,
                remainingToConsume <= 0,
                totalCost,
                averageUnitCost,
                consumed
        );
    }

    public FifoCogsResult simulateFifoCogsStrict(UUID skuId, Integer quantity) {
        FifoCogsResult result = simulateFifoCogs(skuId, quantity);
        if (!result.fullyCovered()) {
            throw new InsufficientStockException(
                    "Insufficient stock for SKU '" + result.skuCode() + "': available "
                            + result.quantityAvailable() + ", requested " + result.quantityRequested());
        }
        return result;
    }

    private BigDecimal averageCost(BigDecimal totalCost, int totalQuantity) {
        if (totalQuantity <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return totalCost.divide(BigDecimal.valueOf(totalQuantity), MONEY_SCALE, RoundingMode.HALF_UP);
    }
}