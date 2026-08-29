package com.rudra.shop.controller;

import com.rudra.shop.model.Order;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.service.RazorpayService;
import com.rudra.shop.service.RevWinAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RazorpayCheckoutController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RevWinAgentService revWinAgentService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam(value = "amount", defaultValue = "0") double amount,
            @RequestParam(value = "receipt", required = false) String receipt) {

        Map<String, Object> response = new HashMap<>();

        if (amount < 1.0) {
            response.put("error", "Amount must be at least ₹1.00 (100 paise)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Map<String, Object> orderData = razorpayService.createOrder(amount, receipt);
            return ResponseEntity.ok(orderData);
        } catch (Exception e) {
            response.put("error", "Failed to create Razorpay order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature,
            @RequestParam(value = "orderNumber", required = false) String orderNumber) {

        Map<String, Object> response = new HashMap<>();

        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            response.put("success", false);
            response.put("message", "Missing required signature verification fields.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (isValid) {
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    order.setStatus("PAID");
                    order.setRazorpayOrderId(razorpayOrderId);
                    order.setRazorpayPaymentId(razorpayPaymentId);
                    orderRepository.save(order);
                }
                revWinAgentService.handlePaymentSuccessWebhook(orderNumber);
            }

            response.put("success", true);
            response.put("message", "Payment verified successfully.");
            response.put("payment_id", razorpayPaymentId);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Payment verification failed: Invalid signature.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
