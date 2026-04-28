package com.urbanvogue.auth.controller;

import com.urbanvogue.auth.dto.RegistrationRequest;
import com.urbanvogue.auth.model.User;
import com.urbanvogue.auth.repository.UserRepository;
import com.urbanvogue.auth.service.AuthService;
import com.urbanvogue.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    // ── REGISTER (Amazon-style: One form creates both auth + profile) ──
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegistrationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Create auth credentials in users table
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole() != null ? request.getRole() : "ROLE_USER");

            User savedUser = authService.register(user);

            // 2. Auto-create user profile in user-service
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("username", request.getUsername());
            profileData.put("fullName", request.getFullName());
            profileData.put("age", request.getAge());
            profileData.put("sex", request.getSex());
            profileData.put("address", request.getAddress());
            profileData.put("email", request.getEmail());

            try {
                String profileUrl = userServiceUrl + "/api/users";
                restTemplate.postForObject(profileUrl, profileData, Object.class);
                System.out.println("✅ User profile created for: " + request.getUsername());
            } catch (Exception e) {
                System.err.println("⚠️ Could not create user profile: " + e.getMessage());
                // Don't fail registration if profile creation fails
            }

            // ── Notify: Send welcome email ──
            try {
                String notifyUrl = notificationServiceUrl +
                        "/api/notifications/welcome?username=" +
                        request.getUsername();
                restTemplate.postForObject(notifyUrl, null, Object.class);
                System.out.println("✅ Welcome notification sent for: " + request.getUsername());
            } catch (Exception e) {
                System.err.println("⚠️ Could not send welcome notification: " + e.getMessage());
            }

            response.put("success", true);
            response.put("message", "Registration successful! Welcome to Urban Vogue.");
            response.put("username", request.getUsername());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
        }

        return response;
    }

    // ── LOGIN — Returns token + user info ──
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository
                    .findByUsername(loginRequest.getUsername())
                    .orElseThrow(() ->
                            new RuntimeException("User not found")
                    );

            if (!passwordEncoder.matches(
                    loginRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid Credentials");
            }

            // ── Generate token with username AND role ──
            String token = jwtUtils.generateToken(
                    user.getUsername(),
                    user.getRole()
            );

            // ── Notify: Send login notification ──
            try {
                String notifyUrl = notificationServiceUrl +
                        "/api/notifications/send";

                Map<String, Object> notificationBody = new HashMap<>();
                notificationBody.put("recipientUsername",
                        user.getUsername());
                notificationBody.put("type", "LOGIN");
                notificationBody.put("channel", "EMAIL");
                notificationBody.put("message",
                        "Hello " + user.getUsername() +
                                "! You have successfully logged in to " +
                                "UrbanVogue. If this wasn't you, " +
                                "please change your password immediately."
                );

                restTemplate.postForObject(
                        notifyUrl, notificationBody, Object.class
                );
                System.out.println(
                        "✅ Login notification sent for: " +
                                user.getUsername()
                );
            } catch (Exception e) {
                System.err.println(
                        "⚠️ Could not send login notification: " +
                        e.getMessage()
                );
            }

            response.put("success", true);
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("userId", user.getId());
            response.put("expiresIn", "72 hours");
            response.put("message", "Login successful!");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

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

    // ── DELETE USER CREDENTIALS (Called by user-service) ──
    @DeleteMapping("/{username}")
    public String deleteUser(@PathVariable String username) {
        authService.deleteUser(username);
        return "Auth credentials deleted for " + username;
    }
}