package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.PurchaseOrderDtos.PurchaseOrderResponse;
import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationRequest;
import org.example.rwandasupplychain.DTOs.QuotationDtos.QuotationResponse;
import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Enums.QuotationStatus;
import org.example.rwandasupplychain.Enums.RfqStatus;
import org.example.rwandasupplychain.Exceptions.InvalidStateTransitionException;
import org.example.rwandasupplychain.Repositories.QuotationRepository;
import org.example.rwandasupplychain.Repositories.RFQRepository;
import org.example.rwandasupplychain.Repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private RFQRepository rfqRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PurchaseOrderService purchaseOrderService;

    private QuotationService quotationService;

    private UUID buyerId;
    private UUID acceptedSupplierId;
    private UUID rivalSupplierId;
    private RFQ rfq;

    @BeforeEach
    void setUp() {
        quotationService = new QuotationService(quotationRepository, rfqRepository, usersRepository,
                notificationService, purchaseOrderService);
        buyerId = UUID.randomUUID();
        acceptedSupplierId = UUID.randomUUID();
        rivalSupplierId = UUID.randomUUID();

        rfq = new RFQ();
        rfq.setId(UUID.randomUUID());
        rfq.setBuyerId(buyerId);
        rfq.setCategory("Maize");
        rfq.setStatus(RfqStatus.OPEN);
    }

    @Test
    void submit_savesQuotationAndNotifiesBuyer() {
        UUID supplierId = UUID.randomUUID();
        QuotationRequest request = new QuotationRequest(rfq.getId(), supplierId, BigDecimal.valueOf(280), 500, "Fresh stock", null);

        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));
        when(usersRepository.existsById(supplierId)).thenReturn(true);
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> {
            Quotation q = inv.getArgument(0);
            q.setId(UUID.randomUUID());
            q.setStatus(QuotationStatus.SUBMITTED);
            return q;
        });

        QuotationResponse response = quotationService.submit(request);

        assertThat(response.status()).isEqualTo(QuotationStatus.SUBMITTED);
        verify(notificationService).notify(eq(buyerId), any(), any(), any(), any());
    }

    @Test
    void submit_rejectsWhenRfqNotOpen() {
        rfq.setStatus(RfqStatus.AWARDED);
        QuotationRequest request = new QuotationRequest(rfq.getId(), UUID.randomUUID(), BigDecimal.valueOf(280), 500, null, null);
        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> quotationService.submit(request))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void submit_rejectsBuyerBiddingOnOwnRfq() {
        QuotationRequest request = new QuotationRequest(rfq.getId(), buyerId, BigDecimal.valueOf(280), 500, null, null);
        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> quotationService.submit(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accept_awardsRfqRejectsRivalsAndCreatesPurchaseOrder() {
        Quotation accepted = quotation(rfq, acceptedSupplierId, QuotationStatus.SUBMITTED);
        Quotation rival = quotation(rfq, rivalSupplierId, QuotationStatus.SUBMITTED);

        when(quotationRepository.findById(accepted.getId())).thenReturn(Optional.of(accepted));
        when(quotationRepository.findByRfq_Id(rfq.getId())).thenReturn(List.of(accepted, rival));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rfqRepository.save(any(RFQ.class))).thenAnswer(inv -> inv.getArgument(0));
        when(purchaseOrderService.createFromQuotation(any(), any())).thenReturn(mock(PurchaseOrderResponse.class));

        QuotationResponse response = quotationService.accept(accepted.getId(), buyerId);

        assertThat(response.status()).isEqualTo(QuotationStatus.ACCEPTED);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.AWARDED);
        assertThat(rival.getStatus()).isEqualTo(QuotationStatus.REJECTED);
        verify(purchaseOrderService).createFromQuotation(accepted, rfq);
    }

    @Test
    void accept_byNonBuyer_isRejected() {
        Quotation quotation = quotation(rfq, acceptedSupplierId, QuotationStatus.SUBMITTED);
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.accept(quotation.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(purchaseOrderService);
    }

    @Test
    void withdraw_byNonSupplier_isRejected() {
        Quotation quotation = quotation(rfq, acceptedSupplierId, QuotationStatus.SUBMITTED);
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.withdraw(quotation.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withdraw_bySupplier_succeeds() {
        Quotation quotation = quotation(rfq, acceptedSupplierId, QuotationStatus.SUBMITTED);
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> inv.getArgument(0));

        QuotationResponse response = quotationService.withdraw(quotation.getId(), acceptedSupplierId);

        assertThat(response.status()).isEqualTo(QuotationStatus.WITHDRAWN);
    }

    private Quotation quotation(RFQ rfq, UUID supplierId, QuotationStatus status) {
        Quotation quotation = new Quotation();
        quotation.setId(UUID.randomUUID());
        quotation.setRfq(rfq);
        quotation.setSupplierId(supplierId);
        quotation.setUnitPrice(BigDecimal.valueOf(280));
        quotation.setQuantity(500);
        quotation.setStatus(status);
        return quotation;
    }
}
