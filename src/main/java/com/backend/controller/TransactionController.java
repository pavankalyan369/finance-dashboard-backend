package com.backend.controller;

import com.backend.dto.transaction.*;
import com.backend.security.CustomUserDetails;
import com.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/me/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService service;

    private UUID getUserId(CustomUserDetails user) {
        return user.getId();
    }

    // CREATE → DTO
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody TransactionCreateRequest request) {

        return ResponseEntity.status(201)
                .body(service.createTransaction(getUserId(user), request));
    }

    // UPDATE → DTO
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request) {

        return ResponseEntity.ok(
                service.updateTransaction(getUserId(user), id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id) {

        service.deleteTransaction(getUserId(user), id);
        return ResponseEntity.noContent().build();
    }

    // GET ALL → DTO PAGE
    @GetMapping
    public ResponseEntity<?> getAll(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok(
                service.getAllTransactions(getUserId(user), page, size, sortBy)
        );
    }

    // GET ONE → DTO
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getOne(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getTransaction(getUserId(user), id)
        );
    }

    // SUMMARY → DTO ONLY
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> summary(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(service.getSummary(getUserId(user)));
    }

    // ANALYTICS
    @GetMapping("/analytics")
    public ResponseEntity<TransactionAnalyticsResponse> analytics(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(service.getAnalytics(getUserId(user)));
    }

    // INSIGHTS
    @GetMapping("/insights")
    public ResponseEntity<TransactionInsightsResponse> insights(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(service.getInsights(getUserId(user)));
    }
}