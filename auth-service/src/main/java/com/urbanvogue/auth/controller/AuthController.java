package com.urbanvogue.auth.controller;

import com.urbanvogue.auth.model.User;
import com.urbanvogue.auth.repository.UserRepository;
import com.urbanvogue.auth.service.AuthService;
import com.urbanvogue.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        authService.register(user);
        return "User registered successfully!";
    }

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the password matches the encrypted one in DB
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Create and return the VIP Pass (JWT)
            return jwtUtils.generateToken(user.getUsername());
        } else {
            throw new RuntimeException("Invalid Credentials");
        }
    }
}