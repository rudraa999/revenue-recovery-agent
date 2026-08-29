package com.rudra.shop.service;

import com.rudra.shop.model.AuditLogEntry;
import com.rudra.shop.model.Order;
import com.rudra.shop.model.PaymentRiskRecord;
import com.rudra.shop.model.RecoveryIntervention;
import com.rudra.shop.repository.AuditLogRepository;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.PaymentRiskRecordRepository;
import com.rudra.shop.repository.RecoveryInterventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RevWinAgentService {

    @Autowired
    private PaymentRiskRecordRepository riskRecordRepository;

    @Autowired
    private RecoveryInterventionRepository interventionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Transactional
    public Map<String, Object> handlePaymentFailureWebhook(String orderNumber, Double amount, String customerName, String customerEmail, String customerPhone, String failureCode, String failureReason) {
        Map<String, Object> result = new HashMap<>();

        // 1. Check if Order exists or create Order
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus("PAYMENT_FAILED");
            order.setFailureCode(failureCode);
            order.setFailureReason(failureReason);
            orderRepository.save(order);
        }

        // 2. Retrieve or Create PaymentRiskRecord
        PaymentRiskRecord riskRecord = riskRecordRepository.findByOrderNumber(orderNumber)
                .orElseGet(() -> {
                    PaymentRiskRecord record = new PaymentRiskRecord();
                    record.setOrderNumber(orderNumber);
                    record.setCustomerName(customerName);
                    record.setCustomerEmail(customerEmail);
                    record.setCustomerPhone(customerPhone);
                    record.setAmount(amount);
                    return record;
                });

        // 3. Stopping Rule Check #1: If already recovered/paid, halt intervention immediately!
        if ("RECOVERED".equalsIgnoreCase(riskRecord.getStatus())) {
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Intervention stopped: Order already successfully paid.", "INFO");
            result.put("status", "STOPPED");
            result.put("message", "Stopping rule triggered: Order already paid.");
            return result;
        }

        // 4. Stopping Rule Check #2: Max retries (Limit to 2 attempts)
        if (riskRecord.getAttemptCount() >= 2) {
            riskRecord.setStatus("ESCALATED");
            riskRecordRepository.save(riskRecord);
            logAudit(orderNumber, "ESCALATION_TRIGGERED", "Stopping rule triggered: Max 2 retries reached. Escalating for human review.", "WARNING");
            result.put("status", "ESCALATED");
            result.put("message", "Max retries reached. Case escalated to Admin.");
            return result;
        }

        // 5. Ingest Event into Audit Ledger
        logAudit(orderNumber, "EVENT_INGESTED", "Ingested payment.failed webhook [Failure Code: " + failureCode + ", Amount: ₹" + amount + "]", "INFO");

        // 6. SCENARIO 1: Root Cause Diagnoser Engine (BANK_GATEWAY_TIMEOUT)
        String diagnosedCause;
        String interventionType;
        String payloadMsg;

        if ("GATEWAY_TIMEOUT".equalsIgnoreCase(failureCode) || "BANK_SERVER_DOWN".equalsIgnoreCase(failureCode)) {
            diagnosedCause = "Bank Gateway Timeout / Network Issuer Outage";
            interventionType = "BACKUP_GATEWAY_LINK";
        } else {
            diagnosedCause = "Issuer Gateway Degradation (" + failureCode + ")";
            interventionType = "BACKUP_GATEWAY_LINK";
        }

        riskRecord.setFailureCode(failureCode);
        riskRecord.setFailureReason(diagnosedCause);
        riskRecord.setRiskType("PAYMENT_FAILURE");
        riskRecord.setStatus("RECOVERING");
        riskRecord.setAttemptCount(riskRecord.getAttemptCount() + 1);

        logAudit(orderNumber, "DIAGNOSED_ROOT_CAUSE", "AI Diagnosis complete: Root Cause = " + diagnosedCause, "INFO");

        // 7. Route Action: Generate Razorpay Smart Recovery Link with Backup Routing
        String recoveryUrl = razorpayService.createSmartRecoveryLink(orderNumber, amount, customerName, customerPhone, failureCode);
        riskRecord.setRecoveryLink(recoveryUrl);
        riskRecordRepository.save(riskRecord);

        payloadMsg = "Hi " + customerName + "! Your artwork order #" + orderNumber + " (₹" + amount + ") was interrupted by a bank gateway timeout. Your items are reserved! Pay instantly via our backup Razorpay route: " + recoveryUrl;

        // 8. Log Intervention
        RecoveryIntervention intervention = new RecoveryIntervention();
        intervention.setRiskRecord(riskRecord);
        intervention.setInterventionType(interventionType);
        intervention.setChannel("RAZORPAY_SMART_LINK");
        intervention.setPayloadMessage(payloadMsg);
        intervention.setRecoveryUrl(recoveryUrl);
        intervention.setAttemptNumber(riskRecord.getAttemptCount());
        intervention.setStatus("EXECUTED");
        interventionRepository.save(intervention);

        logAudit(orderNumber, "INTERVENTION_DISPATCHED", "Routed Action: Dispatched Razorpay Backup Gateway Link -> " + recoveryUrl, "SUCCESS");

        result.put("status", "RECOVERING");
        result.put("orderNumber", orderNumber);
        result.put("amount", amount);
        result.put("diagnosedCause", diagnosedCause);
        result.put("recoveryUrl", recoveryUrl);
        result.put("message", payloadMsg);

        return result;
    }

    @Transactional
    public Map<String, Object> handlePaymentSuccessWebhook(String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        Optional<PaymentRiskRecord> recordOpt = riskRecordRepository.findByOrderNumber(orderNumber);
        if (recordOpt.isPresent()) {
            PaymentRiskRecord record = recordOpt.get();
            record.setStatus("RECOVERED");
            riskRecordRepository.save(record);

            Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus("PAID");
                orderRepository.save(order);
            }

            logAudit(orderNumber, "REVENUE_RECOVERED", "SUCCESS! Payment completed via Razorpay Recovery Link. Won back ₹" + record.getAmount() + "!", "SUCCESS");
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Stopping Rule Triggered: Active recovery halted automatically due to payment success.", "INFO");

            result.put("success", true);
            result.put("orderNumber", orderNumber);
            result.put("amountRecovered", record.getAmount());
            result.put("status", "RECOVERED");
        } else {
            result.put("success", false);
            result.put("message", "Risk record not found.");
        }

        return result;
    }

    private void logAudit(String orderNumber, String eventType, String details, String status) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOrderNumber(orderNumber);
        entry.setEventType(eventType);
        entry.setDetails(details);
        entry.setStatus(status);
        auditLogRepository.save(entry);
    }
}
