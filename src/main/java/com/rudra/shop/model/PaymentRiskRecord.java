package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_risk_records")
@Data
public class PaymentRiskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNumber;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private Double amount;

    private String riskType = "PAYMENT_FAILURE"; // PAYMENT_FAILURE
    private String failureReason; // Bank Server Timeout
    private String failureCode; // GATEWAY_TIMEOUT

    @Column(nullable = false)
    private String status = "AT_RISK"; // AT_RISK, RECOVERING, RECOVERED, ESCALATED, FAILED

    private String recoveryLink;
    private int attemptCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
