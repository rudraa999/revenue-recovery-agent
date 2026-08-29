package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_interventions")
@Data
public class RecoveryIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "risk_record_id", nullable = false)
    private PaymentRiskRecord riskRecord;

    private String interventionType; // BACKUP_GATEWAY_LINK
    private String channel; // RAZORPAY_SMART_LINK

    @Column(columnDefinition = "TEXT")
    private String payloadMessage;

    private String recoveryUrl;
    private int attemptNumber = 1;
    private String status = "EXECUTED"; // EXECUTED, STOPPED, SUCCESS

    private LocalDateTime createdAt = LocalDateTime.now();
}
