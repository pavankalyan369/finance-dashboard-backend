package com.backend.service;

import com.backend.dto.RegisterRequest;
import com.backend.entity.*;
import com.backend.exception.*;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new BadRequestException("User already inactive");
        }

        user.setStatus(AccountStatus.INACTIVE);
        user.setDeactivatedBy(DeactivationReason.ADMIN);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        return "User deactivated";
    }

    public String createAnalyst(RegisterRequest request) {

        userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .ifPresent(u -> { throw new BadRequestException("User already exists"); });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AccountStatus.INACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        Role role = roleRepository.findByName("ANALYST")
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

        return "Analyst created and activation email sent";
    }

    public String deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

// PERMANENT DELETE
        user.setStatus(AccountStatus.DELETED);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeactivatedBy(DeactivationReason.ADMIN);

// invalidate tokens
        user.setTokenVersion(user.getTokenVersion() + 1);


        userRepository.save(user);

        return "User deleted by admin";
    }

    public String assignRole(UUID userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (user.getRoles().contains(role)) {
            throw new BadRequestException("User already has this role");
        }

        user.getRoles().add(role);
        userRepository.save(user);

        return "Role assigned successfully";
    }

    public String removeRole(UUID userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!user.getRoles().contains(role)) {
            throw new BadRequestException("User does not have this role");
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        return "Role removed successfully";
    }

}