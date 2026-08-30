package com.rudra.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudra.shop.dto.CartItemDto;
import com.rudra.shop.model.Order;
import com.rudra.shop.model.User;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.ProductRepository;
import com.rudra.shop.repository.UserRepository;
import com.rudra.shop.service.CartSessionService;
import com.rudra.shop.service.PromoCodeService;
import com.rudra.shop.service.RevWinAgentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private CartSessionService cartSessionService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevWinAgentService revWinAgentService;

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private com.rudra.shop.repository.PaymentRiskRecordRepository riskRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/checkout")
    public String showCheckoutPage(
            @RequestParam(value = "promo", required = false) String promoCodeParam,
            HttpSession session,
            Authentication authentication,
            Model model) {

        List<CartItemDto> cartItems = cartSessionService.getCart(session);
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/products";
        }

        double subtotal = cartSessionService.getCartTotal(session);
        double shippingFee = 0.0;

        String appliedPromo = promoCodeParam != null && !promoCodeParam.trim().isEmpty()
                ? promoCodeParam.trim()
                : (String) session.getAttribute("APPLIED_PROMO_CODE");

        double discount = 0.0;
        if (appliedPromo != null && !appliedPromo.trim().isEmpty()) {
            Map<String, Object> promoRes = promoCodeService.validateAndApplyPromo(appliedPromo, subtotal, null);
            if (Boolean.TRUE.equals(promoRes.get("valid"))) {
                discount = (Double) promoRes.get("discountAmount");
                appliedPromo = (String) promoRes.get("code");
                session.setAttribute("APPLIED_PROMO_CODE", appliedPromo);
                session.setAttribute("APPLIED_DISCOUNT_AMOUNT", discount);
            } else {
                appliedPromo = null;
                session.removeAttribute("APPLIED_PROMO_CODE");
                session.removeAttribute("APPLIED_DISCOUNT_AMOUNT");
            }
        }

        double grandTotal = Math.max(0.0, subtotal + shippingFee - discount);

        String defaultName = "";
        String defaultEmail = "";
        String defaultPhone = "";

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(authentication.getName());
            if (user != null) {
                defaultName = user.getUsername();
                defaultEmail = user.getEmail() != null ? user.getEmail() : "";
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("discount", discount);
        model.addAttribute("appliedPromo", appliedPromo);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("defaultName", defaultName);
        model.addAttribute("defaultEmail", defaultEmail);
        model.addAttribute("defaultPhone", defaultPhone);

        return "checkout";
    }

    @PostMapping("/checkout/buy-now")
    public String buyNowDirect(@RequestParam("productId") Long productId,
                               @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                               HttpSession session) {
        cartSessionService.clearCart(session);
        cartSessionService.addToCart(session, productId, quantity);
        return "redirect:/checkout";
    }

    @PostMapping("/checkout/initiate")
    @ResponseBody
    public Map<String, Object> initiateCheckout(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerEmail") String customerEmail,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam(value = "promoCode", required = false) String promoCode,
            HttpSession session,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        List<CartItemDto> cartItems = cartSessionService.getCart(session);

        if (cartItems == null || cartItems.isEmpty()) {
            response.put("success", false);
            response.put("message", "Your cart is empty.");
            return response;
        }

        double subtotal = cartSessionService.getCartTotal(session);
        double discount = 0.0;
        String appliedPromo = promoCode != null && !promoCode.trim().isEmpty() ? promoCode.trim() : (String) session.getAttribute("APPLIED_PROMO_CODE");

        if (appliedPromo != null && !appliedPromo.trim().isEmpty()) {
            Map<String, Object> promoRes = promoCodeService.validateAndApplyPromo(appliedPromo, subtotal, null);
            if (Boolean.TRUE.equals(promoRes.get("valid"))) {
                discount = (Double) promoRes.get("discountAmount");
                appliedPromo = (String) promoRes.get("code");
            } else {
                appliedPromo = null;
            }
        }

        double finalAmount = Math.max(1.0, subtotal - discount);
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setOriginalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setPromoCode(appliedPromo);
        order.setTotalAmount(finalAmount);
        order.setStatus("PENDING_PAYMENT");

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(authentication.getName());
            if (user != null) {
                order.setUser(user);
            }
        }

        try {
            order.setItemsJson(objectMapper.writeValueAsString(cartItems));
        } catch (Exception e) {
            order.setItemsJson("[]");
        }

        Order savedOrder = orderRepository.save(order);

        response.put("success", true);
        response.put("orderNumber", savedOrder.getOrderNumber());
        response.put("orderId", savedOrder.getId());
        response.put("amount", finalAmount);
        response.put("originalAmount", subtotal);
        response.put("discountAmount", discount);
        response.put("promoCode", appliedPromo);
        response.put("customerName", customerName);
        response.put("customerEmail", customerEmail);
        response.put("customerPhone", customerPhone);

        return response;
    }

    @PostMapping("/checkout/fail-and-recover")
    @ResponseBody
    public Map<String, Object> simulateFailureAndRecover(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerEmail") String customerEmail,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam(value = "promoCode", required = false) String promoCode,
            @RequestParam(value = "failureCode", defaultValue = "GATEWAY_TIMEOUT") String failureCode,
            HttpSession session,
            Authentication authentication) {

        Map<String, Object> initRes = initiateCheckout(customerName, customerEmail, customerPhone, promoCode, session, authentication);
        if (!Boolean.TRUE.equals(initRes.get("success"))) {
            return initRes;
        }

        String orderNumber = (String) initRes.get("orderNumber");
        Double amount = (Double) initRes.get("amount");

        Map<String, Object> agentResponse = revWinAgentService.handlePaymentFailureWebhook(
                orderNumber,
                amount,
                customerName,
                customerEmail,
                customerPhone,
                failureCode,
                "Simulated failure: " + failureCode
        );

        agentResponse.put("success", true);
        agentResponse.put("orderNumber", orderNumber);
        agentResponse.put("amount", amount);

        return agentResponse;
    }

    @PostMapping("/checkout/complete-recovery")
    @ResponseBody
    public Map<String, Object> completeRecovery(@RequestParam("orderNumber") String orderNumber) {
        return revWinAgentService.handlePaymentSuccessWebhook(orderNumber);
    }

    @GetMapping("/checkout/recovery/{orderNumber}")
    public String showRecoveryPage(
            @PathVariable("orderNumber") String orderNumber,
            @RequestParam(value = "promo", required = false) String promo,
            Model model) {

        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
        }

        riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc(orderNumber)
                .ifPresent(record -> {
                    model.addAttribute("riskRecord", record);
                    if (record.getAppliedPromoCode() != null) {
                        model.addAttribute("appliedPromo", record.getAppliedPromoCode());
                        model.addAttribute("discountAmount", record.getDiscountAmount());
                    }
                });

        if (promo != null && !promo.trim().isEmpty()) {
            model.addAttribute("promoParam", promo.trim());
        }

        return "recovery";
    }

    @GetMapping("/checkout/success/{orderNumber}")
    public String orderSuccess(@PathVariable("orderNumber") String orderNumber, Model model, HttpSession session) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            cartSessionService.clearCart(session);
            session.removeAttribute("APPLIED_PROMO_CODE");
            session.removeAttribute("APPLIED_DISCOUNT_AMOUNT");
            return "success";
        }
        return "redirect:/";
    }
}
