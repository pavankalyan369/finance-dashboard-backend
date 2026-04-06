package com.backend.service;

import com.backend.dto.analytics.TopTransactionDTO;
import com.backend.dto.analytics.TransactionSummaryDTO;
import com.backend.entity.Transaction;
import com.backend.entity.TransactionCategory;
import com.backend.entity.TransactionType;
import com.backend.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repository;

    // GET ALL DATA (BASE)
    private List<Transaction> getFiltered(String userId) {
        return repository.findAll().stream()
                .filter(t -> userId == null || t.getUserId().toString().equals(userId))
                .collect(Collectors.toList());
    }

    // SUMMARY
    public TransactionSummaryDTO getSummary(String userId, LocalDate start, LocalDate end) {

        List<Transaction> list = getFiltered(userId);

        BigDecimal income = list.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = list.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionSummaryDTO(income, expense, income.subtract(expense));
    }

    //  TOP TRANSACTIONS
    public Page<TopTransactionDTO> getTopTransactions(
            String userId,
            TransactionCategory category,
            Pageable pageable
    ) {

        List<TopTransactionDTO> list = getFiltered(userId).stream()
                .filter(t -> category == null || t.getCategory() == category)
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .map(t -> new TopTransactionDTO(
                        t.getId(),
                        t.getAmount(),
                        t.getCategory(),
                        t.getCreatedAt().toLocalDate()
                ))
                .toList();

        return toPage(list, pageable);
    }

    // RECENT TRANSACTIONS
    public Page<TopTransactionDTO> getRecentTransactions(String userId, Pageable pageable) {

        List<TopTransactionDTO> list = getFiltered(userId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(t -> new TopTransactionDTO(
                        t.getId(),
                        t.getAmount(),
                        t.getCategory(),
                        t.getCreatedAt().toLocalDate()
                ))
                .toList();

        return toPage(list, pageable);
    }

    //  CATEGORY SUMMARY
    public Map<String, BigDecimal> getCategorySummary(String userId) {

        return getFiltered(userId).stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.mapping(
                                Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    // COUNT
    public Long getTransactionCount(String userId) {
        return (long) getFiltered(userId).size();
    }

    // Pagination helper
    private Page<TopTransactionDTO> toPage(List<TopTransactionDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}