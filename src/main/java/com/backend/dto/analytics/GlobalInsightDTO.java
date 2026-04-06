package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GlobalInsightDTO {
    // analyst
    private Long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
}
