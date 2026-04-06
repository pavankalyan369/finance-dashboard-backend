package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserInsightDTO {
    // analyst
    private String userId;
    private Long totalTransactions;
    private BigDecimal avgAmount;
}
