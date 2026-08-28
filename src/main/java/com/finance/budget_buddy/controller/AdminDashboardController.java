package com.finance.budget_buddy.controller;

import com.finance.budget_buddy.repository.AiReportRepository;
import com.finance.budget_buddy.repository.TransactionRepository;
import com.finance.budget_buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AiReportRepository aiReportRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("transactionCount", transactionRepository.count());
        model.addAttribute("reportCount", aiReportRepository.count());
        model.addAttribute("recentTransactions", transactionRepository.findAll().stream().limit(10).toList());
        return "admin-dashboard";
    }
}
