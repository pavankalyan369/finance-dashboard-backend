package com.backend.service;

import com.backend.exception.AuthException;
import com.backend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.dto.*;
import com.backend.entity.*;
import com.backend.repository.*;
import com.backend.security.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ActivationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;

// REGISTER
    public AuthResponse register(RegisterRequest request) {

        userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .ifPresent(u -> {
                    throw new AuthException(
                            "User already exists. Please sign in | http://localhost:8080/auth/login"
                    );
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AccountStatus.INACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        Role role = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setRoles(Set.of(role));
        userRepository.save(user);


        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .age(request.getAge())
                .gender(request.getGender())
                .occupation(request.getOccupation())
                .build();

        profileRepository.save(profile);


        //  DELETE OLD TOKENS
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        ActivationToken activation = ActivationToken.builder()
                .user(user)
                .token(token)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(activation);

        emailService.sendActivationEmail(user.getEmail(), token);

        return new AuthResponse("Account created. Please activate via email.");
    }

    // ACTIVATE ACCOUNT (UPDATED)
    public String activate(String token) {

        ActivationToken activation = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        //  CHECK USED
        if (activation.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        //  CHECK EXPIRY
        if (activation.getExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = activation.getUser(); //  FIXED

        user.setStatus(AccountStatus.ACTIVE);
        user.setDeactivatedBy(null); //  IMPORTANT

        userRepository.save(user);

        // MARK TOKEN USED
        activation.setUsed(true);
        tokenRepository.save(activation);

        return "Account activated successfully";
    }

    //  LOGIN (UNCHANGED BUT CLEANED)
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        // STEP 1: Validate password FIRST
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }


//  ADMIN PERMANENT DELETE
        if (user.getStatus() == AccountStatus.DELETED &&
                user.getDeactivatedBy() == DeactivationReason.ADMIN) {
            throw new AuthException("Your account has been deleted by admin.");
        }

// USER PERMANENT DELETE (AFTER 30 DAYS)
        if (user.getStatus() == AccountStatus.DELETED &&
                user.getDeactivatedBy() == DeactivationReason.USER) {
            throw new AuthException("Your account has been permanently deleted.");
        }

//  USER SOFT DELETE (WITHIN 30 DAYS)
        if (user.getStatus() == AccountStatus.INACTIVE &&
                user.getDeactivatedBy() == DeactivationReason.USER &&
                user.getDeletedAt() != null &&
                user.getDeletedAt().isAfter(LocalDateTime.now().minusDays(30))) {

            throw new AuthException(
                    "Account deactivated. Recover via /auth/login/reactivate"
            );
        }

        // 🟢 FIRST TIME (USER + ANALYST)
        if (user.getStatus() == AccountStatus.INACTIVE &&
                user.getDeactivatedBy() == null) {

            throw new AuthException(
                    "Activate your account | http://localhost:8080/auth/login/activate"
            );
        }

        // 🟡 DEACTIVATED (USER / SYSTEM)
        if (user.getStatus() == AccountStatus.INACTIVE &&
                user.getDeactivatedBy() != null) {

            throw new AuthException(
                    "Account is deactivated, activate it | http://localhost:8080/auth/login/reactivate"
            );
        }


        //  SUCCESS LOGIN
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                new CustomUserDetails(user),
                user.getTokenVersion()
        );

        return new AuthResponse(token);
    }
// -------------

    public String reactivate(String token) {

        ActivationToken activation = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException("Invalid token"));

        if (activation.isUsed())
            throw new AuthException("Token already used");

        if (activation.getExpiry().isBefore(LocalDateTime.now()))
            throw new AuthException("Token expired");

        User user = activation.getUser();

        //  ADMIN BLOCK
        if (user.getDeactivatedBy() == DeactivationReason.ADMIN) {
            throw new AuthException("Admin deleted accounts cannot be reactivated");
        }

        //  PERMANENT DELETE
        if (user.getStatus() == AccountStatus.DELETED) {
            throw new AuthException("Account permanently deleted");
        }

        //  NOT IN SOFT DELETE
        if (user.getDeletedAt() == null) {
            throw new AuthException("Account is not recoverable");
        }

        //  AFTER 30 DAYS
        if (user.getDeletedAt().isBefore(LocalDateTime.now().minusDays(30))) {
            throw new AuthException("Reactivation period expired");
        }

        // REACTIVATE
        user.setStatus(AccountStatus.ACTIVE);
        user.setDeletedAt(null);
        user.setDeactivatedBy(null);
        user.setDeleted(false); // ensure

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        activation.setUsed(true);
        tokenRepository.save(activation);

        return "Account reactivated successfully";
    }

    public String sendReactivationLink(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDeactivatedBy() == DeactivationReason.ADMIN) {
            throw new RuntimeException("Admin blocked account cannot be reactivated");
        }

        // DELETE OLD TOKENS
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        ActivationToken activation = ActivationToken.builder()
                .user(user)
                .token(token)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(activation);

        emailService.sendReactivationEmail(email, token);

        return "Reactivation link sent";
    }

    public String sendActivationFromLogin(AuthRequest request) {

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }

        //  DELETE OLD TOKENS
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        ActivationToken activation = ActivationToken.builder()
                .user(user)
                .token(token)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(activation);

        emailService.sendActivationEmail(user.getEmail(), token);

        return "Activation link sent";
    }


    public String sendReactivationFromLogin(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }

        if (user.getDeletedAt() == null) {
            throw new AuthException("Account is not deactivated");
        }

        if (user.getDeactivatedBy() == DeactivationReason.ADMIN) {
            throw new AuthException("Account blocked");
        }

        //  DELETE OLD TOKENS
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        ActivationToken activation = ActivationToken.builder()
                .user(user)
                .token(token)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(activation);

        emailService.sendReactivationEmail(user.getEmail(), token);

        return "Reactivation link sent";
    }
}