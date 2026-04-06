package com.backend.dto.analytics;

import com.backend.entity.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TopTransactionDTO {

    private UUID id;
    private BigDecimal amount;
    private TransactionCategory category;
    private LocalDate date;
}