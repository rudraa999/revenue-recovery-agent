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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
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

        if (riskRecords != null && !riskRecords.isEmpty()) {
            totalAtRisk = riskRecords.stream()
                    .filter(r -> r.getAmount() != null)
                    .mapToDouble(PaymentRiskRecord::getAmount).sum();

            totalRecovered = riskRecords.stream()
                    .filter(r -> "RECOVERED".equalsIgnoreCase(r.getStatus()) && r.getAmount() != null)
                    .mapToDouble(PaymentRiskRecord::getAmount).sum();

            recoveredCount = riskRecords.stream()
                    .filter(r -> "RECOVERED".equalsIgnoreCase(r.getStatus())).count();
        }

        double recoveryRate = (riskRecords == null || riskRecords.isEmpty()) ? 0.0 : (double) recoveredCount / riskRecords.size() * 100.0;

        model.addAttribute("riskRecords", riskRecords != null ? riskRecords : new ArrayList<>());
        model.addAttribute("interventions", interventions != null ? interventions : new ArrayList<>());
        model.addAttribute("auditLogs", auditLogs != null ? auditLogs : new ArrayList<>());
        model.addAttribute("totalAtRisk", String.format(java.util.Locale.US, "%.2f", totalAtRisk));
        model.addAttribute("totalRecovered", String.format(java.util.Locale.US, "%.2f", totalRecovered));
        model.addAttribute("recoveryRate", String.format(java.util.Locale.US, "%.1f", recoveryRate));

        return "admin/revenue-recovery";
    }

    @PostMapping("/admin/revenue-recovery/send-reminder")
    @ResponseBody
    public Map<String, Object> sendReminder(@RequestParam("orderNumber") String orderNumber) {
        return revWinAgentService.triggerManualReminder(orderNumber);
    }
}
