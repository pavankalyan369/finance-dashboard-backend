package com.backend.repository;

import com.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<Transaction, UUID> {
}