package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationRequest;
import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationResponse;
import org.example.rwandasupplychain.Entities.Negotiation;
import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Enums.NegotiationRole;
import org.example.rwandasupplychain.Enums.QuotationStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Repositories.NegotiationRepository;
import org.example.rwandasupplychain.Repositories.QuotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NegotiationServiceTest {

    @Mock
    private NegotiationRepository negotiationRepository;
    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private NotificationService notificationService;

    private NegotiationService negotiationService;

    private UUID buyerId;
    private UUID supplierId;
    private Quotation quotation;

    @BeforeEach
    void setUp() {
        negotiationService = new NegotiationService(negotiationRepository, quotationRepository, notificationService);
        buyerId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        RFQ rfq = new RFQ();
        rfq.setId(UUID.randomUUID());
        rfq.setBuyerId(buyerId);

        quotation = new Quotation();
        quotation.setId(UUID.randomUUID());
        quotation.setRfq(rfq);
        quotation.setSupplierId(supplierId);
        quotation.setStatus(QuotationStatus.SUBMITTED);
    }

    @Test
    void addMessage_bySupplier_movesQuotationToUnderNegotiationAndNotifiesBuyer() {
        NegotiationRequest request = new NegotiationRequest(quotation.getId(), supplierId, NegotiationRole.SUPPLIER,
                BigDecimal.valueOf(290), 500, "Can you meet at 290?");

        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(negotiationRepository.save(any(Negotiation.class))).thenAnswer(inv -> {
            Negotiation n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        NegotiationResponse response = negotiationService.addMessage(request);

        assertThat(response.senderRole()).isEqualTo(NegotiationRole.SUPPLIER);
        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.UNDER_NEGOTIATION);
        verify(notificationService).notify(eq(buyerId), any(), any(), any(), any());
    }

    @Test
    void addMessage_senderNotMatchingRole_isRejected() {
        NegotiationRequest request = new NegotiationRequest(quotation.getId(), UUID.randomUUID(), NegotiationRole.SUPPLIER,
                null, null, "not the real supplier");
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> negotiationService.addMessage(request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(negotiationRepository, notificationService);
    }

    @Test
    void addMessage_onDecidedQuotation_isRejected() {
        quotation.setStatus(QuotationStatus.ACCEPTED);
        NegotiationRequest request = new NegotiationRequest(quotation.getId(), buyerId, NegotiationRole.BUYER,
                BigDecimal.valueOf(270), null, "too late");
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> negotiationService.addMessage(request))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
