package com.rudra.shop.service;

import com.rudra.shop.model.PromoCode;
import com.rudra.shop.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    public Map<String, Object> validateAndApplyPromo(String code, double orderTotal, String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        if (code == null || code.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "Please enter a valid coupon code.");
            return result;
        }

        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());
        if (promoOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "Invalid or expired promo code.");
            return result;
        }

        PromoCode promo = promoOpt.get();

        // 1. Expiration check
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            result.put("valid", false);
            result.put("message", "This promo code has expired.");
            return result;
        }

        // 2. Max uses check
        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            result.put("valid", false);
            result.put("message", "This promo code has reached its maximum usage limit.");
            return result;
        }

        // 3. Minimum order amount check
        if (promo.getMinOrderAmount() != null && orderTotal < promo.getMinOrderAmount()) {
            result.put("valid", false);
            result.put("message", String.format("Minimum order amount of ₹%.2f required to use this promo code.", promo.getMinOrderAmount()));
            return result;
        }

        // 4. Target order check for recovery-exclusive codes
        if (promo.isRecoveryExclusive() && promo.getTargetOrderNumber() != null && orderNumber != null) {
            if (!promo.getTargetOrderNumber().equalsIgnoreCase(orderNumber.trim())) {
                result.put("valid", false);
                result.put("message", "This exclusive discount code is not valid for this order.");
                return result;
            }
        }

        // Calculate discount
        double discount = 0.0;
        if ("PERCENTAGE".equalsIgnoreCase(promo.getDiscountType())) {
            discount = (orderTotal * promo.getDiscountValue()) / 100.0;
            if (promo.getMaxDiscountAmount() != null && discount > promo.getMaxDiscountAmount()) {
                discount = promo.getMaxDiscountAmount();
            }
        } else {
            discount = promo.getDiscountValue();
        }

        if (discount > orderTotal) {
            discount = orderTotal;
        }

        double newTotal = Math.max(0.0, orderTotal - discount);

        result.put("valid", true);
        result.put("code", promo.getCode().toUpperCase());
        result.put("discountAmount", discount);
        result.put("discountType", promo.getDiscountType());
        result.put("discountValue", promo.getDiscountValue());
        result.put("newTotal", newTotal);
        result.put("message", String.format("Promo code %s applied successfully! You saved ₹%.2f.", promo.getCode().toUpperCase(), discount));

        return result;
    }

    @Transactional
    public void recordUsage(String code) {
        if (code != null && !code.trim().isEmpty()) {
            promoCodeRepository.findByCodeIgnoreCase(code.trim()).ifPresent(promo -> {
                promo.setUsedCount(promo.getUsedCount() + 1);
                promoCodeRepository.save(promo);
            });
        }
    }

    @Transactional
    public PromoCode generateRecoveryPromoCode(String orderNumber, double discountPercentage) {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String code = "WINBACK" + (int)discountPercentage + "-" + suffix;

        PromoCode promo = new PromoCode();
        promo.setCode(code);
        promo.setDescription("Exclusive " + (int)discountPercentage + "% recovery discount for Order #" + orderNumber);
        promo.setDiscountType("PERCENTAGE");
        promo.setDiscountValue(discountPercentage);
        promo.setMinOrderAmount(1000.0);
        promo.setMaxDiscountAmount(5000.0);
        promo.setMaxUses(1);
        promo.setUsedCount(0);
        promo.setActive(true);
        promo.setRecoveryExclusive(true);
        promo.setTargetOrderNumber(orderNumber);
        promo.setExpiryDate(LocalDateTime.now().plusDays(2)); // Valid for 48 hours

        return promoCodeRepository.save(promo);
    }
}
