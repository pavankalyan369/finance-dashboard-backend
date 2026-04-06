package com.backend.dto.transaction;

import com.backend.entity.TransactionCategory;
import com.backend.entity.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionUpdateRequest {

    @Positive(message = "Amount must be greater than 0")
    @DecimalMax(value = "10000000", message = "Amount too large")
    private Double amount;

    private TransactionCategory category;

    private TransactionType type;

    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    private String description;

    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;
}
