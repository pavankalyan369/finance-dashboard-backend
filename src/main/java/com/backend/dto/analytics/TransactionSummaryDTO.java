package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionSummaryDTO {
    // analyst
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal balance;
}
