package com.rudra.shop.controller;

import com.rudra.shop.service.RevWinAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class PaymentWebhookController {

    @Autowired
    private RevWinAgentService revWinAgentService;

    @PostMapping("/api/webhooks/razorpay")
    public ResponseEntity<Map<String, Object>> handleRazorpayWebhook(@RequestBody Map<String, Object> payload) {
        String event = (String) payload.getOrDefault("event", "payment.failed");
        Map<String, Object> paymentPayload = (Map<String, Object>) payload.getOrDefault("payload", new HashMap<>());
        Map<String, Object> paymentEntity = (Map<String, Object>) paymentPayload.getOrDefault("payment", new HashMap<>());
        Map<String, Object> entity = (Map<String, Object>) paymentEntity.getOrDefault("entity", payload);

        String orderNumber = (String) entity.getOrDefault("order_id", "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Double amount = entity.containsKey("amount") ? Double.parseDouble(entity.get("amount").toString()) : 4500.0;
        String customerName = (String) entity.getOrDefault("customer_name", "Shruti Sharma");
        String customerEmail = (String) entity.getOrDefault("customer_email", "shruti@example.com");
        String customerPhone = (String) entity.getOrDefault("customer_phone", "+919876543210");
        String failureCode = (String) entity.getOrDefault("failure_code", "GATEWAY_TIMEOUT");
        String failureReason = (String) entity.getOrDefault("failure_reason", "Bank Issuer Gateway Timeout");

        if ("payment.authorized".equals(event) || "payment_link.paid".equals(event)) {
            Map<String, Object> result = revWinAgentService.handlePaymentSuccessWebhook(orderNumber);
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> result = revWinAgentService.handlePaymentFailureWebhook(orderNumber, amount, customerName, customerEmail, customerPhone, failureCode, failureReason);
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/api/recovery/simulate-failure")
    public ResponseEntity<Map<String, Object>> simulateFailure(
            @RequestParam(value = "orderNumber", required = false) String orderNumber,
            @RequestParam(value = "amount", defaultValue = "4500.0") Double amount,
            @RequestParam(value = "customerName", defaultValue = "Shruti Sharma") String customerName,
            @RequestParam(value = "customerEmail", defaultValue = "shruti@example.com") String customerEmail,
            @RequestParam(value = "customerPhone", defaultValue = "+919876543210") String customerPhone,
            @RequestParam(value = "failureCode", defaultValue = "GATEWAY_TIMEOUT") String failureCode) {

        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Map<String, Object> result = revWinAgentService.handlePaymentFailureWebhook(
                orderNumber,
                amount,
                customerName,
                customerEmail,
                customerPhone,
                failureCode,
                "Simulated Root Cause: " + failureCode
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/recovery/simulate-gateway-timeout")
    public ResponseEntity<Map<String, Object>> simulateGatewayTimeout(
            @RequestParam(value = "orderNumber", required = false) String orderNumber,
            @RequestParam(value = "amount", defaultValue = "4500.0") Double amount,
            @RequestParam(value = "customerName", defaultValue = "Shruti Sharma") String customerName,
            @RequestParam(value = "customerEmail", defaultValue = "shruti@example.com") String customerEmail,
            @RequestParam(value = "customerPhone", defaultValue = "+919876543210") String customerPhone) {

        return simulateFailure(orderNumber, amount, customerName, customerEmail, customerPhone, "GATEWAY_TIMEOUT");
    }

    @PostMapping("/api/recovery/simulate-upi-error")
    public ResponseEntity<Map<String, Object>> simulateUpiError(
            @RequestParam(value = "orderNumber", required = false) String orderNumber,
            @RequestParam(value = "amount", defaultValue = "5200.0") Double amount,
            @RequestParam(value = "customerName", defaultValue = "Aarav Mehta") String customerName,
            @RequestParam(value = "customerEmail", defaultValue = "aarav@example.com") String customerEmail,
            @RequestParam(value = "customerPhone", defaultValue = "+919811223344") String customerPhone) {

        return simulateFailure(orderNumber, amount, customerName, customerEmail, customerPhone, "UPI_DECLINED");
    }

    @PostMapping("/api/recovery/simulate-insufficient-funds")
    public ResponseEntity<Map<String, Object>> simulateInsufficientFunds(
            @RequestParam(value = "orderNumber", required = false) String orderNumber,
            @RequestParam(value = "amount", defaultValue = "12500.0") Double amount,
            @RequestParam(value = "customerName", defaultValue = "Rohan Verma") String customerName,
            @RequestParam(value = "customerEmail", defaultValue = "rohan@example.com") String customerEmail,
            @RequestParam(value = "customerPhone", defaultValue = "+919822334455") String customerPhone) {

        return simulateFailure(orderNumber, amount, customerName, customerEmail, customerPhone, "INSUFFICIENT_FUNDS");
    }

    @PostMapping("/api/recovery/simulate-advance-cadence")
    public ResponseEntity<Map<String, Object>> simulateAdvanceCadence(@RequestParam("orderNumber") String orderNumber) {
        Map<String, Object> result = revWinAgentService.triggerManualReminder(orderNumber);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/recovery/simulate-payment-success")
    public ResponseEntity<Map<String, Object>> simulatePaymentSuccess(@RequestParam("orderNumber") String orderNumber) {
        Map<String, Object> result = revWinAgentService.handlePaymentSuccessWebhook(orderNumber);
        return ResponseEntity.ok(result);
    }
}
