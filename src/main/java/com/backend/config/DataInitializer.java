package com.backend.config;

import com.backend.entity.AccountStatus;
import com.backend.entity.Profile;
import com.backend.entity.Role;
import com.backend.entity.User;
import com.backend.repository.ProfileRepository;
import com.backend.repository.RoleRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               RoleRepository roleRepo,
                               PasswordEncoder encoder,
                               ProfileRepository profileRepository) {

        return args -> {

            System.out.println("Initializing default roles and users...");

            // 1. CREATE ROLES
            Role adminRole = createRoleIfNotExists(roleRepo, "ADMIN");
            Role customerRole = createRoleIfNotExists(roleRepo, "CUSTOMER");
            Role analystRole = createRoleIfNotExists(roleRepo, "ANALYST");

            //  CREATE USERS

            // ADMIN
            createUserIfNotExists(
                    userRepo,
                    encoder,
                    profileRepository,
                    "admin@gmail.com",
                    "admin123",
                    Set.of(adminRole)
            );

            // ANALYST
            createUserIfNotExists(
                    userRepo,
                    encoder,
                    profileRepository,
                    "analyst@gmail.com",
                    "analyst123",
                    Set.of(analystRole)
            );

            // CUSTOMER
            createUserIfNotExists(
                    userRepo,
                    encoder,
                    profileRepository,
                    "customer@gmail.com",
                    "customer123",
                    Set.of(customerRole)
            );

            // HYBRID (ANALYST + CUSTOMER)
            Set<Role> hybridRoles = new HashSet<>();
            hybridRoles.add(analystRole);
            hybridRoles.add(customerRole);

            createUserIfNotExists(
                    userRepo,
                    encoder,
                    profileRepository,
                    "hybrid@gmail.com",
                    "hybrid123",
                    hybridRoles
            );

            System.out.println("Default roles & users initialized successfully!");
        };
    }

    // HELPER: CREATE ROLE
    private Role createRoleIfNotExists(RoleRepository roleRepo, String roleName) {
        return roleRepo.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepo.save(role);
                });
    }

    // HELPER: CREATE USER + PROFILE
    private void createUserIfNotExists(UserRepository userRepo,
                                       PasswordEncoder encoder,
                                       ProfileRepository profileRepository,
                                       String email,
                                       String rawPassword,
                                       Set<Role> roles) {

        // If user already exists → skip
        if (userRepo.existsByEmail(email)) {
            System.out.println(" User already exists: " + email);
            return;
        }

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRoles(roles);

        userRepo.save(user);

        // Create profile (VERY IMPORTANT)
        Profile profile = Profile.builder()
                .user(user)
                .fullName("Default User")
                .phone("0000000000")
                .address("N/A")
                .age(25)
                .build();

        profileRepository.save(profile);

        System.out.println("Created user: " + email);
    }
}