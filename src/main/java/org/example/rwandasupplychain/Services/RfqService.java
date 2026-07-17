package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.RfqDtos.RfqRequest;
import org.example.rwandasupplychain.DTOs.RfqDtos.RfqResponse;
import org.example.rwandasupplychain.DTOs.SupplierMatchDtos.SupplierMatch;
import org.example.rwandasupplychain.Entities.NotificationType;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Entities.RfqStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.RFQRepository;
import org.example.rwandasupplychain.Repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RfqService {

    private static final int MAX_MATCH_ALERTS = 10;

    private final RFQRepository rfqRepository;
    private final UsersRepository usersRepository;
    private final SupplierMatchingService supplierMatchingService;
    private final NotificationService notificationService;

    public RfqService(RFQRepository rfqRepository,
                       UsersRepository usersRepository,
                       SupplierMatchingService supplierMatchingService,
                       NotificationService notificationService) {
        this.rfqRepository = rfqRepository;
        this.usersRepository = usersRepository;
        this.supplierMatchingService = supplierMatchingService;
        this.notificationService = notificationService;
    }

    public RfqResponse create(RfqRequest request) {
        if (!usersRepository.existsById(request.buyerId())) {
            throw new ResourceNotFoundException("Buyer not found: " + request.buyerId());
        }

        RFQ rfq = new RFQ();
        applyRequest(rfq, request);
        RFQ saved = rfqRepository.save(rfq);

        alertMatchingSuppliers(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RfqResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getAll(UUID buyerId, RfqStatus status, String category) {
        List<RFQ> rfqs;
        if (buyerId != null) {
            rfqs = rfqRepository.findByBuyerId(buyerId);
        } else if (status != null && category != null) {
            rfqs = rfqRepository.findByStatusAndCategoryIgnoreCase(status, category);
        } else if (status != null) {
            rfqs = rfqRepository.findByStatus(status);
        } else {
            rfqs = rfqRepository.findAll();
        }
        return rfqs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getMatchesForSupplier(UUID supplierId) {
        return supplierMatchingService.findOpenRfqsForSupplier(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierMatch> getRecommendations(UUID rfqId, int limit) {
        return supplierMatchingService.findMatches(findEntity(rfqId), limit);
    }

    public RfqResponse cancel(UUID id, UUID requesterId) {
        RFQ rfq = findEntity(id);
        if (!rfq.getBuyerId().equals(requesterId)) {
            throw new IllegalArgumentException("Only the buyer who created this RFQ can cancel it");
        }
        if (rfq.getStatus() != RfqStatus.OPEN) {
            throw new InvalidStateTransitionException("Only an OPEN RFQ can be cancelled, current status: " + rfq.getStatus());
        }
        rfq.setStatus(RfqStatus.CANCELLED);
        return toResponse(rfqRepository.save(rfq));
    }

    protected RFQ findEntity(UUID id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found: " + id));
    }

    private void alertMatchingSuppliers(RFQ rfq) {
        List<SupplierMatch> matches = supplierMatchingService.findMatches(rfq, MAX_MATCH_ALERTS);
        for (SupplierMatch match : matches) {
            notificationService.notify(
                    match.supplierId(),
                    NotificationType.RFQ_MATCH,
                    "New matching RFQ available",
                    "A buyer is looking for '" + rfq.getCategory() + "' (qty " + rfq.getQuantity() + " " + rfq.getUnit() + ") that matches your products.",
                    rfq.getId()
            );
        }
    }

    private void applyRequest(RFQ rfq, RfqRequest request) {
        rfq.setBuyerId(request.buyerId());
        rfq.setCategory(request.category());
        rfq.setDescription(request.description());
        rfq.setQuantity(request.quantity());
        rfq.setUnit(request.unit());
        rfq.setTargetPrice(request.targetPrice());
        rfq.setDeliveryDistrict(request.deliveryDistrict());
        rfq.setDeliveryLatitude(request.deliveryLatitude());
        rfq.setDeliveryLongitude(request.deliveryLongitude());
        rfq.setBiddingDeadline(request.biddingDeadline());
    }

    private RfqResponse toResponse(RFQ rfq) {
        return new RfqResponse(
                rfq.getId(),
                rfq.getBuyerId(),
                rfq.getCategory(),
                rfq.getDescription(),
                rfq.getQuantity(),
                rfq.getUnit(),
                rfq.getTargetPrice(),
                rfq.getDeliveryDistrict(),
                rfq.getDeliveryLatitude(),
                rfq.getDeliveryLongitude(),
                rfq.getBiddingDeadline(),
                rfq.getStatus(),
                rfq.getCreatedAt(),
                rfq.getUpdatedAt()
        );
    }
}
