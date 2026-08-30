package com.rudra.shop.controller;

import com.rudra.shop.service.CartSessionService;
import com.rudra.shop.service.PromoCodeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/promo")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private CartSessionService cartSessionService;

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyPromo(
            @RequestParam("code") String code,
            @RequestParam(value = "orderTotal", required = false) Double orderTotal,
            @RequestParam(value = "orderNumber", required = false) String orderNumber,
            HttpSession session) {

        double total = orderTotal != null && orderTotal > 0 ? orderTotal : cartSessionService.getCartTotal(session);
        Map<String, Object> result = promoCodeService.validateAndApplyPromo(code, total, orderNumber);

        if (Boolean.TRUE.equals(result.get("valid"))) {
            session.setAttribute("APPLIED_PROMO_CODE", result.get("code"));
            session.setAttribute("APPLIED_DISCOUNT_AMOUNT", result.get("discountAmount"));
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removePromo(HttpSession session) {
        session.removeAttribute("APPLIED_PROMO_CODE");
        session.removeAttribute("APPLIED_DISCOUNT_AMOUNT");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Promo code removed.");
        return ResponseEntity.ok(result);
    }
}
