package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationRequest;
import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationResponse;
import org.example.rwandasupplychain.Entities.Negotiation;
import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Enums.NegotiationRole;
import org.example.rwandasupplychain.Enums.NotificationType;
import org.example.rwandasupplychain.Enums.QuotationStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.NegotiationRepository;
import org.example.rwandasupplychain.Repositories.QuotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class NegotiationService {

    private static final Set<QuotationStatus> NEGOTIABLE = Set.of(QuotationStatus.SUBMITTED, QuotationStatus.UNDER_NEGOTIATION);

    private final NegotiationRepository negotiationRepository;
    private final QuotationRepository quotationRepository;
    private final NotificationService notificationService;

    public NegotiationService(NegotiationRepository negotiationRepository,
                               QuotationRepository quotationRepository,
                               NotificationService notificationService) {
        this.negotiationRepository = negotiationRepository;
        this.quotationRepository = quotationRepository;
        this.notificationService = notificationService;
    }

    public NegotiationResponse addMessage(NegotiationRequest request) {
        Quotation quotation = quotationRepository.findById(request.quotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found: " + request.quotationId()));

        if (!NEGOTIABLE.contains(quotation.getStatus())) {
            throw new InvalidStateTransitionException("Cannot negotiate on a quotation with status " + quotation.getStatus());
        }

        UUID buyerId = quotation.getRfq().getBuyerId();
        UUID supplierId = quotation.getSupplierId();
        boolean senderMatchesRole = (request.senderRole() == NegotiationRole.BUYER && request.senderId().equals(buyerId))
                || (request.senderRole() == NegotiationRole.SUPPLIER && request.senderId().equals(supplierId));
        if (!senderMatchesRole) {
            throw new IllegalArgumentException("senderId does not match senderRole for this quotation");
        }

        Negotiation negotiation = new Negotiation();
        negotiation.setQuotation(quotation);
        negotiation.setSenderId(request.senderId());
        negotiation.setSenderRole(request.senderRole());
        negotiation.setProposedPrice(request.proposedPrice());
        negotiation.setProposedQuantity(request.proposedQuantity());
        negotiation.setMessage(request.message());

        Negotiation saved = negotiationRepository.save(negotiation);

        if (quotation.getStatus() == QuotationStatus.SUBMITTED) {
            quotation.setStatus(QuotationStatus.UNDER_NEGOTIATION);
            quotationRepository.save(quotation);
        }

        UUID recipientId = request.senderRole() == NegotiationRole.BUYER ? supplierId : buyerId;
        notificationService.notify(recipientId, NotificationType.NEGOTIATION_UPDATE,
                "New negotiation message", "There's a new counter-offer on your quotation.", quotation.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NegotiationResponse> getThread(UUID quotationId) {
        return negotiationRepository.findByQuotation_IdOrderByCreatedAtAsc(quotationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private NegotiationResponse toResponse(Negotiation negotiation) {
        return new NegotiationResponse(
                negotiation.getId(),
                negotiation.getQuotation().getId(),
                negotiation.getSenderId(),
                negotiation.getSenderRole(),
                negotiation.getProposedPrice(),
                negotiation.getProposedQuantity(),
                negotiation.getMessage(),
                negotiation.getCreatedAt()
        );
    }
}
