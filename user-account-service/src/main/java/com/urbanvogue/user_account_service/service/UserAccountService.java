package com.urbanvogue.user_account_service.service;

import com.urbanvogue.user_account_service.dto.*;
import com.urbanvogue.user_account_service.model.UserAccount;
import com.urbanvogue.user_account_service.repository.UserAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository repo;

    // Create account
    public UserAccount createAccount(String username, Double balance) {

        if (repo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Account already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setBalance(balance);

        return repo.save(user);
    }

    // Debit (used by payment-service)
    public AccountResponse debit(DebitRequest req) {

        UserAccount user = repo.findByUsername(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBalance() < req.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        user.setBalance(user.getBalance() - req.getAmount());
        repo.save(user);

        return new AccountResponse("Amount deducted", user.getBalance());
    }

    // Credit (refund)
    public AccountResponse credit(DebitRequest req) {

        UserAccount user = repo.findByUsername(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBalance(user.getBalance() + req.getAmount());
        repo.save(user);

        return new AccountResponse("Amount refunded", user.getBalance());
    }

    public UserAccount getAccount(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}