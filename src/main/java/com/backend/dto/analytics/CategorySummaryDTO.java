package com.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategorySummaryDTO {
    // analyst

    private String category;
    private BigDecimal totalAmount;
}
