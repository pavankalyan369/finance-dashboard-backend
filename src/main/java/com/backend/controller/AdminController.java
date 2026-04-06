package com.backend.controller;

import com.backend.dto.RegisterRequest;
import com.backend.entity.*;

import com.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivateUser(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }

    @PostMapping("/create-analyst")
    public ResponseEntity<?> create(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(201)
                .body(adminService.createAnalyst(request));
    }

    @PutMapping("/assign-role/{userId}")
    public ResponseEntity<?> assignRole(
            @PathVariable UUID userId,
            @RequestParam String roleName) {

        return ResponseEntity.ok(adminService.assignRole(userId, roleName));
    }

    @PutMapping("/remove-role/{userId}")
    public ResponseEntity<?> removeRole(
            @PathVariable UUID userId,
            @RequestParam String roleName) {

        return ResponseEntity.ok(adminService.removeRole(userId, roleName));
    }

}