package com.urbanvogue.auth_service.controller;

import com.urbanvogue.auth_service.model.User;
import com.urbanvogue.auth_service.repository.UserRepository;
import com.urbanvogue.auth_service.service.AuthService;
import com.urbanvogue.auth_service.util.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","http://localhost:5175"})

public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;



    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        try {
            User savedUser = authService.register(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "username", savedUser.getUsername(),
                    "role", savedUser.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // LOGIN
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        return Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }

    @PutMapping("/add-upi")
    public ResponseEntity<?> addUpi(
            @RequestParam String username,
            @RequestParam String upiId) {

        return ResponseEntity.ok(authService.addUpi(username, upiId));
    }
    // VALIDATE TOKEN
    @GetMapping("/validate")
    public Map<String, Object> validate(@RequestParam String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            String username = jwtUtils.extractUsername(token);

            response.put("valid", true);
            response.put("username", username);
        } catch (Exception e) {
            response.put("valid", false);
        }

        return response;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @GetMapping("/users/username")
    public ResponseEntity<?> getByUsername(@RequestParam String username) {
        return ResponseEntity.ok(authService.getUserByUsername(username));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.deleteById(id));
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteByUsername(@RequestParam String username) {
        return ResponseEntity.ok(authService.deleteByUsername(username));
    }
}