package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId // VERY IMPORTANT
    @JoinColumn(name = "user_id")
    private User user;

    private String fullName;
    private String phone;
    private String address;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Occupation occupation;
}