package com.rudra.shop.service;

import com.rudra.shop.model.Order;
import com.rudra.shop.model.PaymentRiskRecord;
import com.rudra.shop.model.PromoCode;
import com.rudra.shop.repository.AuditLogRepository;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.PaymentRiskRecordRepository;
import com.rudra.shop.repository.RecoveryInterventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevWinAgentServiceTest {

    @Mock
    private PaymentRiskRecordRepository riskRecordRepository;

    @Mock
    private RecoveryInterventionRepository interventionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private PromoCodeService promoCodeService;

    @InjectMocks
    private RevWinAgentService revWinAgentService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testStoppingRuleWhenOrderAlreadyPaid() {
        PaymentRiskRecord paidRecord = new PaymentRiskRecord();
        paidRecord.setOrderNumber("ORD-100");
        paidRecord.setStatus("RECOVERED");

        when(riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc("ORD-100"))
                .thenReturn(Optional.of(paidRecord));

        Map<String, Object> result = revWinAgentService.handlePaymentFailureWebhook(
                "ORD-100", 5000.0, "John", "john@example.com", "9876543210", "GATEWAY_TIMEOUT", "Timeout");

        assertEquals("STOPPED", result.get("status"));
        assertTrue(((String) result.get("message")).contains("already paid"));
    }

    @Test
    void testStoppingRuleWhenCustomerPlacedOtherPaidOrder() {
        PaymentRiskRecord record = new PaymentRiskRecord();
        record.setOrderNumber("ORD-101");
        record.setCustomerEmail("john@example.com");
        record.setCustomerPhone("9876543210");
        record.setStatus("AT_RISK");
        record.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc("ORD-101"))
                .thenReturn(Optional.of(record));
        when(orderRepository.existsByCustomerEmailAndStatusAndOrderNumberNot("john@example.com", "PAID", "ORD-101"))
                .thenReturn(true);

        Map<String, Object> result = revWinAgentService.handlePaymentFailureWebhook(
                "ORD-101", 5000.0, "John", "john@example.com", "9876543210", "GATEWAY_TIMEOUT", "Timeout");

        assertEquals("STOPPED", result.get("status"));
        assertTrue(((String) result.get("message")).contains("placed another order"));
    }

    @Test
    void testStage3CadenceForOrderAbove10kGeneratesPromoCode() {
        PaymentRiskRecord record = new PaymentRiskRecord();
        record.setOrderNumber("ORD-HIGH-VAL");
        record.setCustomerName("VIP Customer");
        record.setCustomerEmail("vip@example.com");
        record.setCustomerPhone("9999999999");
        record.setAmount(15000.0);
        record.setStatus("RECOVERING");
        record.setCadenceStage(2); // Currently at Stage 2, next is Stage 3

        when(orderRepository.findByOrderNumber("ORD-HIGH-VAL")).thenReturn(Optional.empty());

        PromoCode mockPromo = new PromoCode();
        mockPromo.setCode("WINBACK10-ABC123");
        when(promoCodeService.generateRecoveryPromoCode("ORD-HIGH-VAL", 10.0)).thenReturn(mockPromo);

        Map<String, Object> result = revWinAgentService.advanceCadence(record);

        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("cadenceStage"));
        assertEquals("WINBACK10-ABC123", result.get("appliedPromoCode"));
        assertTrue(((String) result.get("message")).contains("WINBACK10-ABC123"));
        verify(promoCodeService, times(1)).generateRecoveryPromoCode("ORD-HIGH-VAL", 10.0);
    }
}
