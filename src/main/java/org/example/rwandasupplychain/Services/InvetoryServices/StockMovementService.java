package org.example.rwandasupplychain.Services.InvetoryServices;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.StockMovementDtos.StockMovementRequest;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.StockMovementDtos.StockMovementResponse;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Batch;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.BatchStatus;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.MovementType;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.StockMovement;
import org.example.rwandasupplychain.Exceptions.InsufficientStockException;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.BatchRepository;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class StockMovementService {

    private static final Set<MovementType> INBOUND = Set.of(MovementType.STOCK_IN, MovementType.RETURNED);
    private static final Set<MovementType> OUTBOUND = Set.of(MovementType.STOCK_OUT, MovementType.DAMAGED, MovementType.TRANSFER);

    private final StockMovementRepository stockMovementRepository;
    private final BatchRepository batchRepository;
    private final BatchService batchService;

    public StockMovementService(StockMovementRepository stockMovementRepository,
                                BatchRepository batchRepository,
                                BatchService batchService) {
        this.stockMovementRepository = stockMovementRepository;
        this.batchRepository = batchRepository;
        this.batchService = batchService;
    }

    public StockMovementResponse record(StockMovementRequest request) {
        Batch batch = batchService.findEntity(request.batchId());

        if (INBOUND.contains(request.type())) {
            batch.setQuantity(batch.getQuantity() + request.quantity());
        } else if (OUTBOUND.contains(request.type())) {
            if (batch.getQuantity() < request.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock in batch '" + batch.getBatchNo() + "': available "
                                + batch.getQuantity() + ", requested " + request.quantity());
            }
            batch.setQuantity(batch.getQuantity() - request.quantity());
        } else {
            batch.setQuantity(request.quantity());
        }

        if (batch.getQuantity() == 0 && batch.getStatus() == BatchStatus.ACTIVE) {
            batch.setStatus(BatchStatus.DEPLETED);
        } else if (batch.getQuantity() > 0 && batch.getStatus() == BatchStatus.DEPLETED) {
            batch.setStatus(BatchStatus.ACTIVE);
        }
        batchRepository.save(batch);

        StockMovement movement = new StockMovement();
        movement.setBatch(batch);
        movement.setType(request.type());
        movement.setQuantity(request.quantity());
        movement.setReason(request.reason());
        movement.setPerformedBy(request.performedBy());

        StockMovement saved = stockMovementRepository.save(movement);
        return toResponse(saved, batch.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getHistory(UUID batchId) {
        return stockMovementRepository.findByBatch_IdOrderByMovementDateDesc(batchId).stream()
                .map(m -> toResponse(m, null))
                .toList();
    }

    private StockMovementResponse toResponse(StockMovement movement, Integer batchQuantityAfter) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getBatch().getId(),
                movement.getBatch().getBatchNo(),
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getMovementDate(),
                batchQuantityAfter
        );
    }
}