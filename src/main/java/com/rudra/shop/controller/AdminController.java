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

import java.util.ArrayList;
import java.util.List;

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
        model.addAttribute("totalAtRisk", totalAtRisk);
        model.addAttribute("totalRecovered", totalRecovered);
        model.addAttribute("recoveryRate", recoveryRate);

        return "admin/revenue-recovery";
    }

    @PostMapping("/revenue-recovery/seed-demo")
    public String seedDemoScenario1() {
        try {
            revWinAgentService.handlePaymentFailureWebhook(
                    "ORD-4500-GATEWAY",
                    4500.0,
                    "Shruti Sharma",
                    "shruti@example.com",
                    "+919822080206",
                    "GATEWAY_TIMEOUT",
                    "Bank Issuer Gateway Timeout (Network Downtime)"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/revenue-recovery";
    }
}
