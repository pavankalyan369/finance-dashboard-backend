package com.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.backend.dto.*;
import com.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // REGISTER
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // ACTIVATE (EMAIL LINK)
    @GetMapping("/activate")
    public String activate(@RequestParam String token) {
        return authService.activate(token);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    // ACTIVATE FROM LOGIN
    @PostMapping("/login/activate")
    public String activateLogin(@RequestBody AuthRequest request) {
        return authService.sendActivationFromLogin(request);
    }

    // REACTIVATE FROM LOGIN
    @PostMapping("/login/reactivate")
    public String reactivateLogin(@RequestBody AuthRequest request) {
        return authService.sendReactivationFromLogin(request);
    }

    // MOST IMPORTANT FIX (EMAIL LINK HANDLER)
    @GetMapping("/reactivate")
    public String reactivate(@RequestParam String token) {
        return authService.reactivate(token);
    }
}