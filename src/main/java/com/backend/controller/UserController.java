package com.backend.controller;


import com.backend.dto.ProfileUpdateRequest;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // READ (CUSTOMER + ANALYST)
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER','ANALYST')")
    public ResponseEntity<?> profile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    // ANALYST BLOCKED
    @PutMapping("/profile/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody ProfileUpdateRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(auth.getName(), request)
        );
    }

    // ANALYST BLOCKED
    @PutMapping("/deactivate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deactivate(Authentication auth) {
        userService.deactivate(auth.getName());
        return ResponseEntity.ok("Account deactivated");
    }

    // ANALYST BLOCKED
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> delete(Authentication auth) {
        userService.delete(auth.getName());
        return ResponseEntity.ok("Account deleted");
    }
}