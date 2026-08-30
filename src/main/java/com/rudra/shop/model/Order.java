package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private Double totalAmount;
    private Double originalAmount;
    private Double discountAmount = 0.0;
    private String promoCode;

    @Column(nullable = false)
    private String status = "PENDING_PAYMENT"; // PENDING_PAYMENT, PAYMENT_FAILED, RECOVERING, RECOVERED, PAID, CANCELLED

    private String failureReason;
    private String failureCode;
    private String paymentMethod;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String recoveryLink;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
