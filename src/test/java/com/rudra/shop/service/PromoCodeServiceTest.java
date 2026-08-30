package com.rudra.shop.service;

import com.rudra.shop.model.PromoCode;
import com.rudra.shop.repository.PromoCodeRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private PromoCodeService promoCodeService;

    private PromoCode percentPromo;
    private PromoCode fixedPromo;

    @BeforeEach
    void setUp() {
        percentPromo = new PromoCode();
        percentPromo.setId(1L);
        percentPromo.setCode("WELCOME10");
        percentPromo.setDiscountType("PERCENTAGE");
        percentPromo.setDiscountValue(10.0);
        percentPromo.setMinOrderAmount(500.0);
        percentPromo.setMaxDiscountAmount(1000.0);
        percentPromo.setMaxUses(100);
        percentPromo.setUsedCount(0);
        percentPromo.setActive(true);
        percentPromo.setExpiryDate(LocalDateTime.now().plusDays(30));

        fixedPromo = new PromoCode();
        fixedPromo.setId(2L);
        fixedPromo.setCode("ART50");
        fixedPromo.setDiscountType("FIXED");
        fixedPromo.setDiscountValue(50.0);
        fixedPromo.setMinOrderAmount(200.0);
        fixedPromo.setMaxUses(100);
        fixedPromo.setUsedCount(0);
        fixedPromo.setActive(true);
        fixedPromo.setExpiryDate(LocalDateTime.now().plusDays(30));
    }

    @Test
    void testValidPercentagePromo() {
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("WELCOME10")).thenReturn(Optional.of(percentPromo));

        Map<String, Object> result = promoCodeService.validateAndApplyPromo("WELCOME10", 2000.0, null);

        assertTrue((Boolean) result.get("valid"));
        assertEquals(200.0, (Double) result.get("discountAmount"));
        assertEquals(1800.0, (Double) result.get("newTotal"));
    }

    @Test
    void testValidFixedPromo() {
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("ART50")).thenReturn(Optional.of(fixedPromo));

        Map<String, Object> result = promoCodeService.validateAndApplyPromo("ART50", 500.0, null);

        assertTrue((Boolean) result.get("valid"));
        assertEquals(50.0, (Double) result.get("discountAmount"));
        assertEquals(450.0, (Double) result.get("newTotal"));
    }

    @Test
    void testMinOrderAmountNotMet() {
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("WELCOME10")).thenReturn(Optional.of(percentPromo));

        Map<String, Object> result = promoCodeService.validateAndApplyPromo("WELCOME10", 300.0, null);

        assertFalse((Boolean) result.get("valid"));
        assertTrue(((String) result.get("message")).contains("Minimum order amount"));
    }

    @Test
    void testExpiredPromoCode() {
        percentPromo.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("WELCOME10")).thenReturn(Optional.of(percentPromo));

        Map<String, Object> result = promoCodeService.validateAndApplyPromo("WELCOME10", 1000.0, null);

        assertFalse((Boolean) result.get("valid"));
        assertEquals("This promo code has expired.", result.get("message"));
    }

    @Test
    void testMaxDiscountCapped() {
        percentPromo.setMaxDiscountAmount(150.0);
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("WELCOME10")).thenReturn(Optional.of(percentPromo));

        Map<String, Object> result = promoCodeService.validateAndApplyPromo("WELCOME10", 5000.0, null);

        assertTrue((Boolean) result.get("valid"));
        assertEquals(150.0, (Double) result.get("discountAmount"));
        assertEquals(4850.0, (Double) result.get("newTotal"));
    }

    @Test
    void testGenerateRecoveryPromoCode() {
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(i -> i.getArgument(0));

        PromoCode generated = promoCodeService.generateRecoveryPromoCode("ORD-123456", 10.0);

        assertNotNull(generated);
        assertTrue(generated.getCode().startsWith("WINBACK10-"));
        assertEquals(10.0, generated.getDiscountValue());
        assertTrue(generated.isRecoveryExclusive());
        assertEquals("ORD-123456", generated.getTargetOrderNumber());
    }
}
