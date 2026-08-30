package com.rudra.shop.service;

import com.rudra.shop.model.AuditLogEntry;
import com.rudra.shop.model.Order;
import com.rudra.shop.model.PaymentRiskRecord;
import com.rudra.shop.model.PromoCode;
import com.rudra.shop.model.RecoveryIntervention;
import com.rudra.shop.repository.AuditLogRepository;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.PaymentRiskRecordRepository;
import com.rudra.shop.repository.RecoveryInterventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private PromoCodeService promoCodeService;

    @Transactional
    public Map<String, Object> handlePaymentFailureWebhook(String orderNumber, Double amount, String customerName, String customerEmail, String customerPhone, String failureCode, String failureReason) {
        Map<String, Object> result = new HashMap<>();

        // 1. Check if Order exists or update Order
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus("PAYMENT_FAILED");
            order.setFailureCode(failureCode);
            order.setFailureReason(failureReason);
            orderRepository.save(order);
        }

        // 2. Retrieve or Create PaymentRiskRecord
        PaymentRiskRecord riskRecord = riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc(orderNumber)
                .orElseGet(() -> {
                    PaymentRiskRecord record = new PaymentRiskRecord();
                    record.setOrderNumber(orderNumber);
                    record.setCustomerName(customerName);
                    record.setCustomerEmail(customerEmail);
                    record.setCustomerPhone(customerPhone);
                    record.setAmount(amount);
                    record.setOriginalAmount(amount);
                    record.setCreatedAt(LocalDateTime.now());
                    return record;
                });

        // 3. Stopping Rule Check #1: If current order already paid/recovered, halt!
        if ("RECOVERED".equalsIgnoreCase(riskRecord.getStatus())) {
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Intervention stopped: Order #" + orderNumber + " is already successfully paid.", "INFO");
            result.put("status", "STOPPED");
            result.put("message", "Stopping rule triggered: Order already paid.");
            return result;
        }

        // 4. Ingest Event into Immutable Audit Ledger
        logAudit(orderNumber, "EVENT_INGESTED", "Ingested payment.failed event [Failure Code: " + failureCode + ", Amount: ₹" + amount + "]", "INFO");

        // 5. Granular Root Cause Diagnoser Engine (Professional E-Commerce Classification)
        DiagnosticResult diagnosis = diagnoseFailureCause(failureCode, failureReason);

        riskRecord.setFailureCode(failureCode);
        riskRecord.setFailureReason(diagnosis.diagnosedCause);
        riskRecord.setRiskType("PAYMENT_FAILURE");
        riskRecord.setStatus("RECOVERING");
        riskRecord.setActionTaken(diagnosis.actionDescription);
        riskRecord.setAttemptCount(riskRecord.getAttemptCount() + 1);
        riskRecord.setCadenceStage(0); // Stage 0: Initial Failure Interception
        riskRecord.setLastReminderSentAt(LocalDateTime.now());
        riskRecord.setNextReminderAt(LocalDateTime.now().plusMinutes(5)); // Schedule Stage 1 in 5 minutes

        logAudit(orderNumber, "DIAGNOSED_ROOT_CAUSE", "Diagnosis: " + diagnosis.diagnosedCause + " | Recommended Route: " + diagnosis.actionDescription, "INFO");

        // 6. Generate Razorpay Instant Recovery Link
        String recoveryUrl = razorpayService.createSmartRecoveryLink(orderNumber, amount, customerName, customerPhone, failureCode);
        riskRecord.setRecoveryLink(recoveryUrl);
        riskRecordRepository.save(riskRecord);

        String payloadMsg = String.format("Dear %s, your order #%s (₹%.2f) at The Arts Arcade encountered a %s. Your items have been safely reserved. Complete your order seamlessly here: %s",
                customerName != null ? customerName : "Valued Customer",
                orderNumber,
                amount,
                diagnosis.diagnosedCause,
                recoveryUrl);

        // 7. Dispatch & Log Initial Intervention
        RecoveryIntervention intervention = new RecoveryIntervention();
        intervention.setRiskRecord(riskRecord);
        intervention.setInterventionType(diagnosis.interventionType);
        intervention.setChannel("INSTANT_WEBHOOK_RECOVERY_LINK");
        intervention.setPayloadMessage(payloadMsg);
        intervention.setRecoveryUrl(recoveryUrl);
        intervention.setAttemptNumber(riskRecord.getAttemptCount());
        intervention.setStatus("EXECUTED");
        interventionRepository.save(intervention);

        logAudit(orderNumber, "INTERVENTION_DISPATCHED", "Action: " + diagnosis.actionDescription + " -> " + recoveryUrl, "SUCCESS");

        result.put("status", "RECOVERING");
        result.put("orderNumber", orderNumber);
        result.put("amount", amount);
        result.put("failureCode", failureCode);
        result.put("diagnosedCause", diagnosis.diagnosedCause);
        result.put("actionDescription", diagnosis.actionDescription);
        result.put("recoveryUrl", recoveryUrl);
        result.put("message", payloadMsg);

        return result;
    }

    @Transactional
    public Map<String, Object> handlePaymentSuccessWebhook(String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        Optional<PaymentRiskRecord> recordOpt = riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc(orderNumber);
        if (recordOpt.isPresent()) {
            PaymentRiskRecord record = recordOpt.get();
            record.setStatus("RECOVERED");
            record.setActionTaken("Payment successfully recovered via 1-click fallback link");
            riskRecordRepository.save(record);

            Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus("PAID");
                orderRepository.save(order);
            }

            // Record promo code usage if applied
            if (record.getAppliedPromoCode() != null) {
                promoCodeService.recordUsage(record.getAppliedPromoCode());
            }

            logAudit(orderNumber, "REVENUE_RECOVERED", "SUCCESS! Payment completed for Order #" + orderNumber + ". Won back ₹" + record.getAmount() + "!", "SUCCESS");
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Stopping Rule Triggered: Order successfully paid. All scheduled reminders terminated.", "INFO");

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

    /**
     * Automated Background Engine: Evaluates pending risk records and triggers the next cadence reminder.
     * Runs every 15 seconds.
     */
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void runAutomatedCadenceJob() {
        List<PaymentRiskRecord> pendingRecords = riskRecordRepository.findByStatusIn(Arrays.asList("RECOVERING", "AT_RISK", "REMINDER_SENT"));
        LocalDateTime now = LocalDateTime.now();

        for (PaymentRiskRecord record : pendingRecords) {
            if (record.getNextReminderAt() != null && record.getNextReminderAt().isBefore(now)) {
                advanceCadence(record);
            }
        }
    }

    /**
     * Advances the cadence for a record based on 3-Hour Interval Rules:
     * - Stage 0 -> Stage 1: 5 minutes after failure
     * - Stage 1 -> Stage 2: 3 hours later
     * - Stage 2 -> Stage 3: 3 hours later (Total 6 hrs). If amount >= ₹10k, generate 10% promo code!
     * - Stage 3 -> Stage 4: 3 hours later (Total 9 hrs). Final warning before releasing inventory.
     * - Stage 4 -> Escalate to human review.
     */
    @Transactional
    public Map<String, Object> advanceCadence(PaymentRiskRecord record) {
        Map<String, Object> response = new HashMap<>();
        String orderNumber = record.getOrderNumber();

        // Stopping Rule Check 1: Has current order been paid?
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent() && "PAID".equalsIgnoreCase(orderOpt.get().getStatus())) {
            record.setStatus("RECOVERED");
            record.setActionTaken("Order successfully paid; Scheduled recovery cadence closed");
            riskRecordRepository.save(record);
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Cadence halted: Order #" + orderNumber + " is already paid.", "INFO");
            response.put("success", false);
            response.put("message", "Order already paid. Cadence halted.");
            return response;
        }

        // Stopping Rule Check 2: Has customer placed/paid any OTHER order SINCE the failure occurred?
        if (hasCustomerPlacedOtherPaidOrder(record.getCustomerEmail(), record.getCustomerPhone(), record.getCreatedAt(), orderNumber)) {
            record.setStatus("STOPPED");
            record.setActionTaken("Reminders gracefully halted: Customer completed another purchase");
            riskRecordRepository.save(record);
            logAudit(orderNumber, "STOPPING_RULE_TRIGGERED", "Cadence halted: Customer has completed an alternative purchase. No further reminders dispatched.", "INFO");
            response.put("success", false);
            response.put("message", "Customer placed another order. Cadence halted.");
            return response;
        }

        int currentStage = record.getCadenceStage();
        int nextStage = currentStage + 1;

        String customerName = record.getCustomerName() != null ? record.getCustomerName() : "Customer";
        String customerPhone = record.getCustomerPhone() != null ? record.getCustomerPhone() : "Customer Phone";
        Double amount = record.getAmount() != null ? record.getAmount() : 0.0;
        String recoveryUrl = record.getRecoveryLink() != null ? record.getRecoveryLink() : "/checkout/recovery/" + orderNumber;

        String interventionType;
        String reminderMsg;
        String actionSummary;
        LocalDateTime nextSchedule;

        if (nextStage == 1) {
            // Stage 1 (5 Minutes after failure)
            interventionType = "STAGE_1_5MIN_REMINDER";
            actionSummary = "Dispatched Stage 1 instant recovery link via SMS/WhatsApp";
            reminderMsg = String.format("Hi %s, we noticed your payment for Order #%s (₹%.2f) was interrupted. Your handcrafted items are safely reserved for you! Complete your order securely here: %s",
                    customerName, orderNumber, amount, recoveryUrl);
            nextSchedule = LocalDateTime.now().plusHours(3); // Schedule Stage 2 in 3 hours
        } else if (nextStage == 2) {
            // Stage 2 (3 Hours after Stage 1)
            interventionType = "STAGE_2_3HR_REMINDER";
            actionSummary = "Dispatched Stage 2 priority cart reservation reminder";
            reminderMsg = String.format("Hi %s, your reserved items for Order #%s (₹%.2f) are held in priority queue. Click here to resume instant checkout: %s",
                    customerName, orderNumber, amount, recoveryUrl);
            nextSchedule = LocalDateTime.now().plusHours(3); // Schedule Stage 3 in 3 hours
        } else if (nextStage == 3) {
            // Stage 3 (6 Hours total - 3 hours after Stage 2)
            interventionType = "STAGE_3_6HR_OFFER_REMINDER";

            // SPECIAL INCENTIVE: If order >= 10,000, generate 10% promo code
            if (amount >= 10000.0) {
                PromoCode promo = promoCodeService.generateRecoveryPromoCode(orderNumber, 10.0);
                record.setAppliedPromoCode(promo.getCode());
                record.setDiscountAmount((amount * 10.0) / 100.0);
                actionSummary = "Triggered 10% Win-back coupon (" + promo.getCode() + ") for VIP order";

                reminderMsg = String.format("Special Privilege for %s: We would love to have you onboard! Complete Order #%s and get an EXCLUSIVE 10%% DISCOUNT using coupon %s at checkout (Save ₹%.2f): %s?promo=%s",
                        customerName, orderNumber, promo.getCode(), record.getDiscountAmount(), recoveryUrl, promo.getCode());

                logAudit(orderNumber, "PROMO_INCENTIVE_ATTACHED", "Triggered 10% Win-back Discount for high-value order (>= ₹10,000). Generated Promo Code: " + promo.getCode(), "SUCCESS");
            } else {
                actionSummary = "Dispatched Stage 3 priority shipping offer";
                reminderMsg = String.format("Hi %s, we would love to see your artwork delivered! Complete Order #%s (₹%.2f) today for complimentary priority shipping: %s",
                        customerName, orderNumber, amount, recoveryUrl);
            }
            nextSchedule = LocalDateTime.now().plusHours(3); // Schedule Stage 4 in 3 hours
        } else if (nextStage == 4) {
            // Stage 4 (9 Hours total - Final Reminder)
            interventionType = "STAGE_4_9HR_FINAL_WARNING";
            actionSummary = "Dispatched Stage 4 final reservation expiration warning";
            reminderMsg = String.format("Final Reminder for %s: The reservation on your selected items for Order #%s (₹%.2f) is expiring soon. Click here to complete your payment before inventory is released: %s",
                    customerName, orderNumber, amount, recoveryUrl);
            nextSchedule = null; // No further stages
        } else {
            // Escalation
            record.setStatus("ESCALATED");
            record.setActionTaken("Max retries reached; Escalated to VIP human support desk");
            riskRecordRepository.save(record);
            logAudit(orderNumber, "ESCALATION_TRIGGERED", "All 4 Cadence Stages completed without payment. Escalating Order #" + orderNumber + " to human VIP desk.", "WARNING");

            response.put("success", true);
            response.put("status", "ESCALATED");
            response.put("message", "Cadence completed. Escalated to Admin.");
            return response;
        }

        // Record Intervention
        RecoveryIntervention intervention = new RecoveryIntervention();
        intervention.setRiskRecord(record);
        intervention.setInterventionType(interventionType);
        intervention.setChannel("OMNICHANNEL_SMS_WHATSAPP");
        intervention.setPayloadMessage(reminderMsg);
        intervention.setRecoveryUrl(recoveryUrl);
        intervention.setAttemptNumber(nextStage);
        intervention.setStatus("SENT");
        interventionRepository.save(intervention);

        // Update Record
        record.setCadenceStage(nextStage);
        record.setActionTaken(actionSummary);
        record.setAttemptCount(record.getAttemptCount() + 1);
        record.setLastReminderSentAt(LocalDateTime.now());
        record.setNextReminderAt(nextSchedule);
        record.setStatus(nextStage == 4 ? "FINAL_REMINDER_SENT" : "REMINDER_SENT");
        riskRecordRepository.save(record);

        logAudit(orderNumber, "CADENCE_DISPATCHED", "Dispatched Stage " + nextStage + " Reminder (" + interventionType + ") to " + customerPhone, "INFO");

        response.put("success", true);
        response.put("orderNumber", orderNumber);
        response.put("cadenceStage", nextStage);
        response.put("status", record.getStatus());
        response.put("appliedPromoCode", record.getAppliedPromoCode());
        response.put("message", reminderMsg);

        return response;
    }

    @Transactional
    public Map<String, Object> triggerManualReminder(String orderNumber) {
        Optional<PaymentRiskRecord> recordOpt = riskRecordRepository.findFirstByOrderNumberOrderByCreatedAtDesc(orderNumber);
        if (recordOpt.isPresent()) {
            return advanceCadence(recordOpt.get());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Order risk record not found.");
        return response;
    }

    /**
     * Helper to verify if customer placed any OTHER paid order SINCE the failure occurred
     */
    private boolean hasCustomerPlacedOtherPaidOrder(String email, String phone, LocalDateTime since, String currentOrderNumber) {
        try {
            if (since == null) {
                return false;
            }
            if (email != null && !email.trim().isEmpty()) {
                if (orderRepository.existsByCustomerEmailAndStatusAndCreatedAtAfter(email.trim(), "PAID", since)) {
                    return true;
                }
            }
            if (phone != null && !phone.trim().isEmpty()) {
                if (orderRepository.existsByCustomerPhoneAndStatusAndCreatedAtAfter(phone.trim(), "PAID", since)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore if check fails
        }
        return false;
    }

    /**
     * Professional Failure Diagnoser Engine
     */
    private DiagnosticResult diagnoseFailureCause(String failureCode, String failureReason) {
        if (failureCode == null) failureCode = "";
        failureCode = failureCode.toUpperCase();

        if (failureCode.contains("UPI_DECLINED") || failureCode.contains("UPI_PIN_ERROR")) {
            return new DiagnosticResult("UPI Payment Declined by Bank/PSP", "UPI_DIRECT_INTENT_LINK", "Generated direct 1-click UPI Intent & QR payment route");
        } else if (failureCode.contains("INSUFFICIENT_FUNDS") || failureCode.contains("BANK_BALANCE_LOW") || failureCode.contains("CARD_LIMIT_EXCEEDED")) {
            return new DiagnosticResult("Insufficient Bank Balance / Card Limit Reached", "MULTI_METHOD_LINK", "Offered alternative payment routes (NetBanking / Split Cards / EMI)");
        } else if (failureCode.contains("VPA_INVALID") || failureCode.contains("INVALID_VPA")) {
            return new DiagnosticResult("Invalid UPI Virtual Payment Address (VPA)", "UPI_VPA_CORRECTION_LINK", "Offered 1-click UPI ID verification & alternate UPI Apps");
        } else if (failureCode.contains("GATEWAY_TIMEOUT") || failureCode.contains("BANK_SERVER_DOWN") || failureCode.contains("SERVER_ERROR")) {
            return new DiagnosticResult("Bank Gateway Outage / Server Timeout", "BACKUP_GATEWAY_LINK", "Rerouted transaction via secondary high-availability banking gateway");
        } else if (failureCode.contains("AUTHENTICATION_FAILED") || failureCode.contains("PAYMENT_AUTHENTICATION_FAILED")) {
            return new DiagnosticResult("2-Factor OTP Verification Timeout", "INSTANT_RETRY_LINK", "Generated direct 1-click OTP re-verification session");
        } else if (failureCode.contains("CHECKOUT_DISMISSED")) {
            return new DiagnosticResult("Customer Checkout Session Interrupted", "RESERVED_CART_LINK", "Generated personalized reserved cart instant payment link");
        } else {
            return new DiagnosticResult("Banking Network Interruption", "BACKUP_GATEWAY_LINK", "Generated Razorpay Smart Recovery Link with fallback routing");
        }
    }

    private void logAudit(String orderNumber, String eventType, String details, String status) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOrderNumber(orderNumber);
        entry.setEventType(eventType);
        entry.setDetails(details);
        entry.setStatus(status);
        auditLogRepository.save(entry);
    }

    private static class DiagnosticResult {
        String diagnosedCause;
        String interventionType;
        String actionDescription;

        DiagnosticResult(String diagnosedCause, String interventionType, String actionDescription) {
            this.diagnosedCause = diagnosedCause;
            this.interventionType = interventionType;
            this.actionDescription = actionDescription;
        }
    }
}
