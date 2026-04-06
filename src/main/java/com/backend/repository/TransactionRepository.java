package com.backend.repository;

import com.backend.entity.Transaction;
import com.backend.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    List<Transaction> findByUserIdAndIsDeletedFalse(UUID userId);

    Transaction findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    List<Transaction> findByUserIdAndTypeAndIsDeletedFalse(UUID userId, TransactionType type);
}