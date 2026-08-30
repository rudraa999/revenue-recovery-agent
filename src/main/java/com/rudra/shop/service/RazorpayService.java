package com.rudra.shop.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public Map<String, Object> createOrder(double amountInRupees, String receipt) throws Exception {
        long amountInPaise = Math.round(amountInRupees * 100);
        if (amountInPaise < 100) {
            amountInPaise = 100; // Minimum 100 paise (₹1)
        }

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt != null && !receipt.trim().isEmpty() ? receipt : "receipt_" + System.currentTimeMillis());

        Order order = client.orders.create(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("order_id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("key_id", razorpayKeyId);

        return response;
    }

    public boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
        if (orderId == null || paymentId == null || razorpaySignature == null) {
            return false;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", razorpaySignature);

            return Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (Exception e) {
            // Manual HMAC-SHA256 signature verification fallback
            try {
                String payload = orderId + "|" + paymentId;
                Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret_key = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                sha256_HMAC.init(secret_key);
                byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString().equalsIgnoreCase(razorpaySignature);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public String createSmartRecoveryLink(String orderNumber, Double amount, String customerName, String customerPhone, String failureCode) {
        String linkId = "plink_" + UUID.randomUUID().toString().substring(0, 10);
        return "https://rzp.io/i/" + linkId;
    }
}
