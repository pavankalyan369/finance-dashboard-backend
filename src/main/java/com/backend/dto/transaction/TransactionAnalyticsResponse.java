package com.backend.dto.transaction;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class TransactionAnalyticsResponse {

    private Map<String, BigDecimal> monthlyTrends;
    private Map<String, BigDecimal> categorySummary;
    private BigDecimal averageTransaction;
}