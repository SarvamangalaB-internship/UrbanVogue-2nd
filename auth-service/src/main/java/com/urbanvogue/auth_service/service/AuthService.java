package com.urbanvogue.auth_service.service;

import com.urbanvogue.auth_service.model.User;
import com.urbanvogue.auth_service.repository.UserRepository;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // GET ALL USERS
    public List<User> getAllUsers() {
        return repo.findAll();
    }

    // GET BY ID
    public User getUserById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET BY USERNAME
    public User getUserByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // DELETE BY ID
    public String deleteById(Long id) {
        repo.deleteById(id);
        return "User deleted";
    }

    // DELETE BY USERNAME
    public String deleteByUsername(String username) {
        repo.deleteByUsername(username);
        return "User deleted";
    }

    public User register(User user) {

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new RuntimeException("Username required");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("Email required");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password required");
        }

        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }

        return repo.save(user);
    }
    public String addUpi(String username, String upiId) {

        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUpiId(upiId);

        repo.save(user);

        return "UPI added successfully";
    }
}