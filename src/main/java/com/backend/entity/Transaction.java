package com.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // INCOME / EXPENSE

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    private String notes;

    private LocalDate transactionDate;

    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // 🔥 Automatically update timestamps
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}