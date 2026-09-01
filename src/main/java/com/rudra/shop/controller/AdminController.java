package com.rudra.shop.controller;

import com.rudra.shop.model.AuditLogEntry;
import com.rudra.shop.model.PaymentRiskRecord;
import com.rudra.shop.model.RecoveryIntervention;
import com.rudra.shop.repository.AuditLogRepository;
import com.rudra.shop.repository.PaymentRiskRecordRepository;
import com.rudra.shop.repository.RecoveryInterventionRepository;
import com.rudra.shop.service.RevWinAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PaymentRiskRecordRepository riskRecordRepository;

    @Autowired
    private RecoveryInterventionRepository interventionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RevWinAgentService revWinAgentService;

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/revenue-recovery")
    public String showRecoveryDashboard(Model model) {
        List<PaymentRiskRecord> riskRecords = new ArrayList<>();
        List<RecoveryIntervention> interventions = new ArrayList<>();
        List<AuditLogEntry> auditLogs = new ArrayList<>();

        try {
            riskRecords = riskRecordRepository.findAllByOrderByCreatedAtDesc();
            interventions = interventionRepository.findAllByOrderByCreatedAtDesc();
            auditLogs = auditLogRepository.findTop50ByOrderByCreatedAtDesc();
        } catch (Exception e) {
            e.printStackTrace();
        }

        double totalAtRisk = 0.0;
        double totalRecovered = 0.0;
        long recoveredCount = 0;
        long stoppedCount = 0;
        long escalatedCount = 0;

        if (riskRecords != null && !riskRecords.isEmpty()) {
            totalAtRisk = riskRecords.stream()
                    .filter(r -> r.getAmount() != null)
                    .mapToDouble(PaymentRiskRecord::getAmount).sum();

            totalRecovered = riskRecords.stream()
                    .filter(r -> "RECOVERED".equalsIgnoreCase(r.getStatus()) && r.getAmount() != null)
                    .mapToDouble(PaymentRiskRecord::getAmount).sum();

            recoveredCount = riskRecords.stream()
                    .filter(r -> "RECOVERED".equalsIgnoreCase(r.getStatus())).count();

            stoppedCount = riskRecords.stream()
                    .filter(r -> "STOPPED".equalsIgnoreCase(r.getStatus())).count();

            escalatedCount = riskRecords.stream()
                    .filter(r -> "ESCALATED".equalsIgnoreCase(r.getStatus())).count();
        }

        long totalCount = riskRecords != null ? riskRecords.size() : 0;
        long activeCount = totalCount - recoveredCount - stoppedCount - escalatedCount;
        if (activeCount < 0) activeCount = 0;

        double recoveryRate = (totalCount == 0) ? 0.0 : ((double) recoveredCount / totalCount) * 100.0;
        double avgRecoveredAmount = (recoveredCount == 0) ? 0.0 : (totalRecovered / recoveredCount);
        double pipelineSavedPercentage = (totalAtRisk == 0.0) ? 0.0 : (totalRecovered / totalAtRisk) * 100.0;

        model.addAttribute("riskRecords", riskRecords != null ? riskRecords : new ArrayList<>());
        model.addAttribute("interventions", interventions != null ? interventions : new ArrayList<>());
        model.addAttribute("auditLogs", auditLogs != null ? auditLogs : new ArrayList<>());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("recoveredCount", recoveredCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("pendingCount", totalCount - recoveredCount);
        model.addAttribute("stoppedCount", stoppedCount);
        model.addAttribute("escalatedCount", escalatedCount);
        model.addAttribute("totalAtRisk", String.format(java.util.Locale.US, "%.2f", totalAtRisk));
        model.addAttribute("totalRecovered", String.format(java.util.Locale.US, "%.2f", totalRecovered));
        model.addAttribute("recoveryRate", String.format(java.util.Locale.US, "%.1f", recoveryRate));
        model.addAttribute("avgRecoveredAmount", String.format(java.util.Locale.US, "%.2f", avgRecoveredAmount));
        model.addAttribute("pipelineSavedPercentage", String.format(java.util.Locale.US, "%.1f", pipelineSavedPercentage));

        return "admin/revenue-recovery";
    }

    @PostMapping("/revenue-recovery/send-reminder")
    @ResponseBody
    public Map<String, Object> sendReminder(@RequestParam("orderNumber") String orderNumber) {
        return revWinAgentService.triggerManualReminder(orderNumber);
    }

    @PostMapping("/revenue-recovery/advance-stage")
    @ResponseBody
    public Map<String, Object> advanceStage(@RequestParam("orderNumber") String orderNumber) {
        return revWinAgentService.triggerManualReminder(orderNumber);
    }

    @GetMapping("/revenue-recovery/api/data")
    @ResponseBody
    public Map<String, Object> getRecoveryDataJson() {
        Map<String, Object> data = new HashMap<>();
        List<PaymentRiskRecord> riskRecords = riskRecordRepository.findAllByOrderByCreatedAtDesc();
        List<RecoveryIntervention> interventions = interventionRepository.findAllByOrderByCreatedAtDesc();
        List<AuditLogEntry> auditLogs = auditLogRepository.findTop50ByOrderByCreatedAtDesc();

        data.put("riskRecords", riskRecords);
        data.put("interventions", interventions);
        data.put("auditLogs", auditLogs);
        return data;
    }
}
