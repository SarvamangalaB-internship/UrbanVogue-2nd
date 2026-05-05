package com.urbanvogue.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        // This starts the Auth Service on Port 8082
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}