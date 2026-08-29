package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log_entries")
@Data
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private String eventType; // EVENT_INGESTED, DIAGNOSED_ROOT_CAUSE, INTERVENTION_DISPATCHED, REVENUE_RECOVERED, STOPPING_RULE_TRIGGERED
    
    @Column(columnDefinition = "TEXT")
    private String details;

    private String status = "INFO"; // INFO, WARNING, SUCCESS, ESCALATED

    private LocalDateTime createdAt = LocalDateTime.now();
}
