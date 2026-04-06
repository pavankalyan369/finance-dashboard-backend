package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "activation_tokens")
public class ActivationToken {

    @Id
    @GeneratedValue
    private UUID id;

    //  Proper relation instead of userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiry;

    //  ADD THIS (missing in DB earlier)
    @Column(nullable = false)
    private boolean used = false;

    // OPTIONAL (matches your DB)
    private LocalDateTime createdAt;
}