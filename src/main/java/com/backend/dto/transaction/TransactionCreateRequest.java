package com.backend.dto.transaction;

import com.backend.entity.TransactionCategory;
import com.backend.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionCreateRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @DecimalMax(value = "10000000", message = "Amount too large")
    private Double amount;

    @NotNull(message = "Category is required")
    private TransactionCategory category;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    private String description;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "Type is required")
    private TransactionType type;
}
