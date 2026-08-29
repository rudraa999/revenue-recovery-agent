package com.rudra.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudra.shop.dto.CartItemDto;
import com.rudra.shop.model.Order;
import com.rudra.shop.model.User;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.ProductRepository;
import com.rudra.shop.repository.UserRepository;
import com.rudra.shop.service.CartSessionService;
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
    private com.rudra.shop.repository.PaymentRiskRecordRepository riskRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Authentication authentication, Model model) {
        List<CartItemDto> cartItems = cartSessionService.getCart(session);
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/products";
        }

        double subtotal = cartSessionService.getCartTotal(session);
        double shippingFee = 0.0;
        double grandTotal = subtotal + shippingFee;

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
            HttpSession session,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        List<CartItemDto> cartItems = cartSessionService.getCart(session);

        if (cartItems == null || cartItems.isEmpty()) {
            response.put("success", false);
            response.put("message", "Your cart is empty.");
            return response;
        }

        double totalAmount = cartSessionService.getCartTotal(session);
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setTotalAmount(totalAmount);
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
        response.put("amount", totalAmount);
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
            @RequestParam(value = "failureCode", defaultValue = "GATEWAY_TIMEOUT") String failureCode,
            HttpSession session,
            Authentication authentication) {

        Map<String, Object> initRes = initiateCheckout(customerName, customerEmail, customerPhone, session, authentication);
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
                "Bank Issuer Gateway Timeout (Network Outage)"
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
    public String showRecoveryPage(@PathVariable("orderNumber") String orderNumber, Model model) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            model.addAttribute("order", orderOpt.get());
        }
        riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc(orderNumber)
                .ifPresent(record -> model.addAttribute("riskRecord", record));
        return "recovery";
    }

    @GetMapping("/checkout/success/{orderNumber}")
    public String orderSuccess(@PathVariable("orderNumber") String orderNumber, Model model, HttpSession session) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            cartSessionService.clearCart(session);
            return "success";
        }
        return "redirect:/";
    }
}
