package com.backend.util;

import com.backend.dto.transaction.TransactionCreateRequest;
import com.backend.dto.transaction.TransactionResponseDTO;
import com.backend.dto.transaction.TransactionUpdateRequest;
import com.backend.entity.Transaction;

import java.math.BigDecimal;

public class TransactionMapper {

    public static Transaction toEntity(TransactionCreateRequest dto) {
        Transaction t = new Transaction();
        t.setAmount(BigDecimal.valueOf(dto.getAmount()));
        t.setCategory(dto.getCategory());
        t.setType(dto.getType());
        t.setNotes(dto.getDescription());
        t.setTransactionDate(dto.getDate());
        return t;
    }

    public static void updateEntity(Transaction t, TransactionUpdateRequest dto) {
        t.setAmount(BigDecimal.valueOf(dto.getAmount()));
        t.setCategory(dto.getCategory());
        t.setType(dto.getType());
        t.setNotes(dto.getDescription());
        t.setTransactionDate(dto.getDate());
    }

    //  ENTITY → RESPONSE DTO
    public static TransactionResponseDTO toDTO(Transaction t) {
        return TransactionResponseDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .description(t.getNotes())
                .build();
    }
}