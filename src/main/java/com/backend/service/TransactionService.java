package com.backend.service;

import com.backend.dto.transaction.*;
import com.backend.entity.Transaction;
import com.backend.entity.TransactionType;
import com.backend.exception.BadRequestException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.TransactionRepository;
import com.backend.util.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    // CREATE
    public TransactionResponseDTO createTransaction(UUID userId, TransactionCreateRequest request) {

        Transaction transaction = TransactionMapper.toEntity(request);
        transaction.setUserId(userId);

        return TransactionMapper.toDTO(repository.save(transaction));
    }

    // UPDATE (FULL ONLY)
    public TransactionResponseDTO updateTransaction(UUID userId, UUID id, TransactionUpdateRequest request) {

        if (request.getAmount() == null ||
                request.getCategory() == null ||
                request.getType() == null ||
                request.getDescription() == null ||
                request.getDate() == null) {

            throw new BadRequestException("All fields are required for full update");
        }

        Transaction transaction = repository.findByIdAndUserIdAndIsDeletedFalse(id, userId);

        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        TransactionMapper.updateEntity(transaction, request);

        return TransactionMapper.toDTO(repository.save(transaction));
    }

    //  DELETE
    public void deleteTransaction(UUID userId, UUID id) {

        Transaction transaction = repository.findByIdAndUserIdAndIsDeletedFalse(id, userId);

        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transaction.setDeleted(true);
        transaction.setDeletedAt(LocalDateTime.now());

        repository.save(transaction);
    }

    //  GET ONE
    public TransactionResponseDTO getTransaction(UUID userId, UUID id) {

        Transaction transaction = repository.findByIdAndUserIdAndIsDeletedFalse(id, userId);

        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return TransactionMapper.toDTO(transaction);
    }

    //  GET ALL
    public Map<String, Object> getAllTransactions(
            UUID userId, int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Page<Transaction> transactionPage =
                repository.findByUserIdAndIsDeletedFalse(userId, pageable);

        List<TransactionResponseDTO> data = transactionPage.getContent()
                .stream()
                .map(TransactionMapper::toDTO)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("page", transactionPage.getNumber());
        response.put("size", transactionPage.getSize());
        response.put("totalElements", transactionPage.getTotalElements());
        response.put("totalPages", transactionPage.getTotalPages());
        response.put("hasNext", transactionPage.hasNext());
        response.put("hasPrevious", transactionPage.hasPrevious());

        return response;
    }

    //  FIXED SUMMARY (ACCURATE)
    public TransactionSummaryResponse getSummary(UUID userId) {

        List<Transaction> transactions = repository.findByUserIdAndIsDeletedFalse(userId);

        if (transactions == null || transactions.isEmpty()) {
            return new TransactionSummaryResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {

            if (t.getAmount() == null) continue;

            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        return new TransactionSummaryResponse(
                totalIncome.setScale(2, RoundingMode.HALF_UP),
                totalExpense.setScale(2, RoundingMode.HALF_UP),
                totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP)
        );
    }

    //  ANALYTICS
    public TransactionAnalyticsResponse getAnalytics(UUID userId) {

        List<Transaction> transactions = repository.findByUserIdAndIsDeletedFalse(userId);

        Map<String, BigDecimal> monthlyTrends = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        Map<String, BigDecimal> categorySummary = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = transactions.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);

        TransactionAnalyticsResponse res = new TransactionAnalyticsResponse();
        res.setMonthlyTrends(monthlyTrends);
        res.setCategorySummary(categorySummary);
        res.setAverageTransaction(avg);

        return res;
    }

    // INSIGHTS
    public TransactionInsightsResponse getInsights(UUID userId) {

        List<Transaction> transactions = repository.findByUserIdAndIsDeletedFalse(userId);

        TransactionInsightsResponse res = new TransactionInsightsResponse();

        Optional<Transaction> maxTxn = transactions.stream()
                .max(Comparator.comparing(Transaction::getAmount));

        res.setHighestTransactionAmount(
                maxTxn.map(Transaction::getAmount).orElse(BigDecimal.ZERO)
        );

        Map<String, BigDecimal> categoryTotals = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        res.setHighestSpendingCategory(
                categoryTotals.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A")
        );

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        res.setBudgetAlert(
                totalExpense.compareTo(BigDecimal.valueOf(50000)) > 0
                        ? "Warning: You are spending too much!"
                        : "Your spending is within a healthy range."
        );

        return res;
    }
}