package com.backend.dto.transaction;

import com.backend.entity.TransactionCategory;
import com.backend.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransactionResponseDTO {

    private UUID id;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionCategory category;
    private LocalDate transactionDate;
    private String description;
}
