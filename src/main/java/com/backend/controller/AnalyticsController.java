package com.backend.controller;

import com.backend.dto.analytics.TopTransactionDTO;
import com.backend.dto.analytics.TransactionSummaryDTO;
import com.backend.entity.TransactionCategory;
import com.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ANALYST')")
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/summary")
    public TransactionSummaryDTO getSummary(
            @RequestParam(required = false) String userId
    ) {
        return service.getSummary(userId, null, null);
    }

    @GetMapping("/top-transactions")
    public Page<TopTransactionDTO> getTopTransactions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) TransactionCategory category,
            Pageable pageable
    ) {
        return service.getTopTransactions(userId, category, pageable);
    }

    @GetMapping("/recent-transactions")
    public Page<TopTransactionDTO> getRecentTransactions(
            @RequestParam(required = false) String userId,
            Pageable pageable
    ) {
        return service.getRecentTransactions(userId, pageable);
    }

    @GetMapping("/category-summary")
    public Map<String, BigDecimal> getCategorySummary(
            @RequestParam(required = false) String userId
    ) {
        return service.getCategorySummary(userId);
    }

    @GetMapping("/count")
    public Long getTransactionCount(
            @RequestParam(required = false) String userId
    ) {
        return service.getTransactionCount(userId);
    }
}