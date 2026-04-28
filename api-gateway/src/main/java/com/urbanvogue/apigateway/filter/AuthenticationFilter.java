package com.urbanvogue.apigateway.filter;

import com.urbanvogue.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            
            // Allow public paths
            String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login") || path.equals("/api/products") || path.startsWith("/api/users/exists/")) {
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                jwtUtil.validateToken(authHeader);
                String username = jwtUtil.extractUsername(authHeader);
                
                // Mutate the request to add the username header
                exchange.getRequest().mutate()
                        .header("X-Logged-In-User", username)
                        .build();

            } catch (Exception e) {
                System.out.println("Invalid token: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        });
    }

    public static class Config { }
}
