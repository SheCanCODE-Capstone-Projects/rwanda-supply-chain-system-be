package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationRequest;
import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationResponse;
import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Enums.NotificationType;
import org.example.rwandasupplychain.Enums.QuotationStatus;
import org.example.rwandasupplychain.Enums.RfqStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.QuotationRepository;
import org.example.rwandasupplychain.Repositories.RFQRepository;
import org.example.rwandasupplychain.Repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class QuotationService {

    private static final Set<QuotationStatus> WITHDRAWABLE = Set.of(QuotationStatus.SUBMITTED, QuotationStatus.UNDER_NEGOTIATION);
    private static final Set<QuotationStatus> DECIDABLE = Set.of(QuotationStatus.SUBMITTED, QuotationStatus.UNDER_NEGOTIATION);

    private final QuotationRepository quotationRepository;
    private final RFQRepository rfqRepository;
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;
    private final PurchaseOrderService purchaseOrderService;

    public QuotationService(QuotationRepository quotationRepository,
                             RFQRepository rfqRepository,
                             UsersRepository usersRepository,
                             NotificationService notificationService,
                             PurchaseOrderService purchaseOrderService) {
        this.quotationRepository = quotationRepository;
        this.rfqRepository = rfqRepository;
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
        this.purchaseOrderService = purchaseOrderService;
    }

    public QuotationResponse submit(QuotationRequest request) {
        RFQ rfq = rfqRepository.findById(request.rfqId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found: " + request.rfqId()));
        if (rfq.getStatus() != RfqStatus.OPEN) {
            throw new InvalidStateTransitionException("Cannot submit a quotation for an RFQ that is not OPEN, current status: " + rfq.getStatus());
        }
        if (rfq.getBuyerId().equals(request.supplierId())) {
            throw new IllegalArgumentException("A buyer cannot submit a quotation on their own RFQ");
        }
        if (!usersRepository.existsById(request.supplierId())) {
            throw new ResourceNotFoundException("Supplier not found: " + request.supplierId());
        }

        Quotation quotation = new Quotation();
        quotation.setRfq(rfq);
        quotation.setSupplierId(request.supplierId());
        quotation.setUnitPrice(request.unitPrice());
        quotation.setQuantity(request.quantity());
        quotation.setMessage(request.message());
        quotation.setValidUntil(request.validUntil());

        Quotation saved = quotationRepository.save(quotation);

        notificationService.notify(rfq.getBuyerId(), NotificationType.QUOTATION_SUBMITTED,
                "New quotation received", "A supplier submitted a quotation for your RFQ '" + rfq.getCategory() + "'.", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuotationResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getByRfq(UUID rfqId) {
        return quotationRepository.findByRfq_Id(rfqId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getBySupplier(UUID supplierId) {
        return quotationRepository.findBySupplierId(supplierId).stream().map(this::toResponse).toList();
    }

    public QuotationResponse accept(UUID id, UUID requesterId) {
        Quotation quotation = findEntity(id);
        RFQ rfq = quotation.getRfq();

        if (!rfq.getBuyerId().equals(requesterId)) {
            throw new IllegalArgumentException("Only the buyer who created this RFQ can accept a quotation");
        }
        if (!DECIDABLE.contains(quotation.getStatus())) {
            throw new InvalidStateTransitionException("Cannot accept a quotation with status " + quotation.getStatus());
        }

        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotationRepository.save(quotation);

        rejectRivalQuotations(rfq.getId(), quotation.getId());

        rfq.setStatus(RfqStatus.AWARDED);
        rfqRepository.save(rfq);

        notificationService.notify(quotation.getSupplierId(), NotificationType.QUOTATION_ACCEPTED,
                "Your quotation was accepted", "Your quotation for RFQ '" + rfq.getCategory() + "' has been accepted.", quotation.getId());

        purchaseOrderService.createFromQuotation(quotation, rfq);

        return toResponse(quotation);
    }

    public QuotationResponse reject(UUID id, UUID requesterId) {
        Quotation quotation = findEntity(id);
        RFQ rfq = quotation.getRfq();

        if (!rfq.getBuyerId().equals(requesterId)) {
            throw new IllegalArgumentException("Only the buyer who created this RFQ can reject a quotation");
        }
        if (!DECIDABLE.contains(quotation.getStatus())) {
            throw new InvalidStateTransitionException("Cannot reject a quotation with status " + quotation.getStatus());
        }

        quotation.setStatus(QuotationStatus.REJECTED);
        Quotation saved = quotationRepository.save(quotation);

        notificationService.notify(quotation.getSupplierId(), NotificationType.QUOTATION_REJECTED,
                "Your quotation was rejected", "Your quotation for RFQ '" + rfq.getCategory() + "' was rejected.", quotation.getId());

        return toResponse(saved);
    }

    public QuotationResponse withdraw(UUID id, UUID requesterId) {
        Quotation quotation = findEntity(id);
        if (!quotation.getSupplierId().equals(requesterId)) {
            throw new IllegalArgumentException("Only the supplier who submitted this quotation can withdraw it");
        }
        if (!WITHDRAWABLE.contains(quotation.getStatus())) {
            throw new InvalidStateTransitionException("Cannot withdraw a quotation with status " + quotation.getStatus());
        }

        quotation.setStatus(QuotationStatus.WITHDRAWN);
        return toResponse(quotationRepository.save(quotation));
    }

    protected Quotation findEntity(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found: " + id));
    }

    private void rejectRivalQuotations(UUID rfqId, UUID acceptedQuotationId) {
        quotationRepository.findByRfq_Id(rfqId).stream()
                .filter(q -> !q.getId().equals(acceptedQuotationId))
                .filter(q -> DECIDABLE.contains(q.getStatus()))
                .forEach(rival -> {
                    rival.setStatus(QuotationStatus.REJECTED);
                    quotationRepository.save(rival);
                    notificationService.notify(rival.getSupplierId(), NotificationType.QUOTATION_REJECTED,
                            "Your quotation was rejected", "Another supplier's quotation was accepted for this RFQ.", rival.getId());
                });
    }

    private QuotationResponse toResponse(Quotation quotation) {
        return new QuotationResponse(
                quotation.getId(),
                quotation.getRfq().getId(),
                quotation.getSupplierId(),
                quotation.getUnitPrice(),
                quotation.getQuantity(),
                quotation.getMessage(),
                quotation.getStatus(),
                quotation.getValidUntil(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt()
        );
    }
}
