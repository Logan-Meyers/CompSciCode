package edu.wsu.cpts422.reccenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    PaymentRepository repo;
    PaymentGateway gateway;
    PaymentService service;
    private FeeCalculator feeCalculator;

    @BeforeEach
    void setUp() {
        repo = mock(PaymentRepository.class);
        gateway = mock(PaymentGateway.class);
        feeCalculator = spy(new FeeCalculator());

        service = new PaymentService(new FeeCalculator(), repo, gateway);
    }

    @Test
    @DisplayName("A cash payment is saved")
    void cashPaymentIsSaved() {
        PaymentRequest request = new PaymentRequest("subu", 25, "WA", Plan.MONTHLY, PaymentMethod.CASH, null);

        service.pay(request);

        verify(repo).save(any());
    }

    @Test
    @DisplayName("A card payment is saved")
    void approvedCardHasSurcharged() {
        when(gateway.charge(anyString(), anyLong())).thenReturn(ChargeResult.approved("TXN422"));

        PaymentRequest request = new PaymentRequest("Elliott", 25, "WA", Plan.QUARTERLY, PaymentMethod.CARD, "4242 4242 4242 4242");

        service.pay(request);

        verify(gateway).charge("4242424242424242", 10197L);
        verify(repo).save(any());
    }

    @Test
    @DisplayName("A card payment is saved and has transaction ID")
    void savedCardPaymentHasTransactionID() {
        when(gateway.charge(anyString(), anyLong())).thenReturn(ChargeResult.approved("TXN422"));

        PaymentRequest request = new PaymentRequest("Caleb", 70, "WA", Plan.ANNUAL, PaymentMethod.CARD, "4242 4242 4242 4242");

        service.pay(request);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        verify(repo).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals("TXN422", saved.getTransactionId());
        assertEquals(40000L, captor.getValue().getBaseFeeCents());
    }

    // throw exceptions
    // when(gateway.charge(anyString(), anyLong())).thenThrow(new GatewayTimeoutException("Gateway timed out"));

    // spy - make discount percent return a fixed value for testing, not mocking entire fee class
    // doReturn(10000L).when(feeCalculator).discountPercent(anyInt(), anyString());
}