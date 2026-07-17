package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.PurchaseOrderDtos.PurchaseOrderResponse;
import org.example.rwandasupplychain.Entities.PurchaseOrder;
import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Enums.NotificationType;
import org.example.rwandasupplychain.Enums.PurchaseOrderStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PurchaseOrderService {

    private static final DateTimeFormatter PO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<PurchaseOrderStatus, Set<PurchaseOrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PurchaseOrderStatus.CREATED, Set.of(PurchaseOrderStatus.CONFIRMED, PurchaseOrderStatus.CANCELLED),
            PurchaseOrderStatus.CONFIRMED, Set.of(PurchaseOrderStatus.IN_TRANSIT, PurchaseOrderStatus.CANCELLED),
            PurchaseOrderStatus.IN_TRANSIT, Set.of(PurchaseOrderStatus.DELIVERED),
            PurchaseOrderStatus.DELIVERED, Set.of(),
            PurchaseOrderStatus.CANCELLED, Set.of()
    );

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final NotificationService notificationService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                 NotificationService notificationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.notificationService = notificationService;
    }

    public PurchaseOrderResponse createFromQuotation(Quotation quotation, RFQ rfq) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPoNumber(generatePoNumber());
        purchaseOrder.setRfq(rfq);
        purchaseOrder.setQuotation(quotation);
        purchaseOrder.setBuyerId(rfq.getBuyerId());
        purchaseOrder.setSupplierId(quotation.getSupplierId());
        purchaseOrder.setUnitPrice(quotation.getUnitPrice());
        purchaseOrder.setQuantity(quotation.getQuantity());
        purchaseOrder.setTotalAmount(quotation.getUnitPrice().multiply(BigDecimal.valueOf(quotation.getQuantity())));

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        notificationService.notify(rfq.getBuyerId(), NotificationType.PURCHASE_ORDER_CREATED,
                "Purchase Order created", "Purchase Order " + saved.getPoNumber() + " has been generated.", saved.getId());
        notificationService.notify(quotation.getSupplierId(), NotificationType.PURCHASE_ORDER_CREATED,
                "Purchase Order created", "Purchase Order " + saved.getPoNumber() + " has been generated.", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAll(UUID buyerId, UUID supplierId) {
        List<PurchaseOrder> purchaseOrders;
        if (buyerId != null) {
            purchaseOrders = purchaseOrderRepository.findByBuyerId(buyerId);
        } else if (supplierId != null) {
            purchaseOrders = purchaseOrderRepository.findBySupplierId(supplierId);
        } else {
            purchaseOrders = purchaseOrderRepository.findAll();
        }
        return purchaseOrders.stream().map(this::toResponse).toList();
    }

    public PurchaseOrderResponse updateStatus(UUID id, PurchaseOrderStatus newStatus) {
        PurchaseOrder purchaseOrder = findEntity(id);
        Set<PurchaseOrderStatus> allowed = ALLOWED_TRANSITIONS.get(purchaseOrder.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    "Cannot move Purchase Order from " + purchaseOrder.getStatus() + " to " + newStatus);
        }
        purchaseOrder.setStatus(newStatus);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    private PurchaseOrder findEntity(UUID id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + id));
    }

    private String generatePoNumber() {
        return "PO-" + LocalDateTime.now().format(PO_DATE_FORMAT) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        return new PurchaseOrderResponse(
                purchaseOrder.getId(),
                purchaseOrder.getPoNumber(),
                purchaseOrder.getRfq().getId(),
                purchaseOrder.getQuotation().getId(),
                purchaseOrder.getBuyerId(),
                purchaseOrder.getSupplierId(),
                purchaseOrder.getUnitPrice(),
                purchaseOrder.getQuantity(),
                purchaseOrder.getTotalAmount(),
                purchaseOrder.getStatus(),
                purchaseOrder.getCreatedAt(),
                purchaseOrder.getUpdatedAt()
        );
    }
}
