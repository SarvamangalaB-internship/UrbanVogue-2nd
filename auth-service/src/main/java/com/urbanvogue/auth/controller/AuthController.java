package com.urbanvogue.auth.controller;

import com.urbanvogue.auth.model.User;
import com.urbanvogue.auth.repository.UserRepository;
import com.urbanvogue.auth.service.AuthService;
import com.urbanvogue.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    // ── REGISTER ──
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        authService.register(user);
        return "User registered successfully!";
    }

    // ── LOGIN — Returns token + user info ──
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User loginRequest) {

        User user = userRepository
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!passwordEncoder.matches(
                loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        // Generate token with username AND role
        String token = jwtUtils.generateToken(
                user.getUsername(),
                user.getRole()
        );

        // Return token with helpful info
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("expiresIn", "72 hours");
        response.put("message", "Login successful!");

        return response;
    }

    // ── VALIDATE — Check if token is still valid ──
    @PostMapping("/validate")
    public Map<String, Object> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "No token provided");
            return response;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        if (jwtUtils.validateToken(token)) {
            response.put("valid", true);
            response.put("username",
                    jwtUtils.getUsernameFromToken(token));
            response.put("role",
                    jwtUtils.getRoleFromToken(token));
            response.put("expired", false);
            response.put("message", "Token is valid");
        } else {
            response.put("valid", false);
            response.put("expired", true);
            response.put("message",
                    "Token is expired or invalid. Please login again.");
        }

        return response;
    }
}