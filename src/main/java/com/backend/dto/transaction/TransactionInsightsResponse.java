package com.backend.dto.transaction;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionInsightsResponse {

    private String highestSpendingCategory;
    private BigDecimal highestTransactionAmount;
    private String budgetAlert;
}