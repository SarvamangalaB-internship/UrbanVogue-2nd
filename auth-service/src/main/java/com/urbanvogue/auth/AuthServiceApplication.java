package com.urbanvogue.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        // This starts the Auth Service on Port 8082
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}