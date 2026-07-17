package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.NotificationDtos.NotificationResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RfqServiceTest {

    @Mock
    private RFQRepository rfqRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private SupplierMatchingService supplierMatchingService;
    @Mock
    private NotificationService notificationService;

    private RfqService rfqService;

    private UUID buyerId;
    private UUID supplierId;

    @BeforeEach
    void setUp() {
        rfqService = new RfqService(rfqRepository, usersRepository, supplierMatchingService, notificationService);
        buyerId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
    }

    @Test
    void create_savesRfqAndAlertsMatchingSuppliers() {
        RfqRequest request = new RfqRequest(buyerId, "Maize", "500kg of grade A maize", 500, "kg",
                BigDecimal.valueOf(300), "Kigali", -1.95, 30.06, LocalDateTime.now().plusDays(3));

        when(usersRepository.existsById(buyerId)).thenReturn(true);
        when(rfqRepository.save(any(RFQ.class))).thenAnswer(inv -> {
            RFQ rfq = inv.getArgument(0);
            rfq.setId(UUID.randomUUID());
            rfq.setStatus(RfqStatus.OPEN);
            return rfq;
        });

        SupplierMatch match = new SupplierMatch(supplierId, "Kigali Cooperative", UUID.randomUUID(), "Maize",
                UUID.randomUUID(), "MAIZE-001", BigDecimal.valueOf(280), 12.5, 0.9);
        when(supplierMatchingService.findMatches(any(RFQ.class), anyInt())).thenReturn(List.of(match));
        when(notificationService.notify(any(), any(), any(), any(), any()))
                .thenReturn(mock(NotificationResponse.class));

        RfqResponse response = rfqService.create(request);

        assertThat(response.category()).isEqualTo("Maize");
        assertThat(response.status()).isEqualTo(RfqStatus.OPEN);
        verify(notificationService).notify(eq(supplierId), eq(NotificationType.RFQ_MATCH), any(), any(), any());
    }

    @Test
    void create_rejectsUnknownBuyer() {
        RfqRequest request = new RfqRequest(buyerId, "Maize", null, 100, "kg",
                null, null, null, null, null);
        when(usersRepository.existsById(buyerId)).thenReturn(false);

        assertThatThrownBy(() -> rfqService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(rfqRepository, supplierMatchingService, notificationService);
    }

    @Test
    void cancel_byNonOwner_isRejected() {
        RFQ rfq = new RFQ();
        rfq.setId(UUID.randomUUID());
        rfq.setBuyerId(buyerId);
        rfq.setStatus(RfqStatus.OPEN);
        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> rfqService.cancel(rfq.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancel_nonOpenRfq_isRejected() {
        RFQ rfq = new RFQ();
        rfq.setId(UUID.randomUUID());
        rfq.setBuyerId(buyerId);
        rfq.setStatus(RfqStatus.AWARDED);
        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> rfqService.cancel(rfq.getId(), buyerId))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancel_openRfqByOwner_succeeds() {
        RFQ rfq = new RFQ();
        rfq.setId(UUID.randomUUID());
        rfq.setBuyerId(buyerId);
        rfq.setStatus(RfqStatus.OPEN);
        when(rfqRepository.findById(rfq.getId())).thenReturn(Optional.of(rfq));
        when(rfqRepository.save(any(RFQ.class))).thenAnswer(inv -> inv.getArgument(0));

        RfqResponse response = rfqService.cancel(rfq.getId(), buyerId);

        assertThat(response.status()).isEqualTo(RfqStatus.CANCELLED);
    }
}
