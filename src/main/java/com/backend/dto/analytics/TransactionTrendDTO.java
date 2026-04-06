package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransactionTrendDTO {
    // analyst
    private LocalDate date;
    private BigDecimal totalAmount;
}
