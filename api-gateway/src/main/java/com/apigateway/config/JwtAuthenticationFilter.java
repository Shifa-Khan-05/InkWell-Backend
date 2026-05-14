package com.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Allow CORS preflight requests
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // 1. Public Paths (Always allowed, no JWT needed)
        boolean isPublicPath = path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/send-otp") ||
                path.contains("/auth/forgot-password") ||
                path.contains("/auth/reset-password") ||
                path.contains("/login") ||
                path.contains("/oauth2") ||
                path.contains("/swagger") ||
                path.contains("/api-docs") ||
                path.contains("/uploads") ||
                path.contains("/post_uploads") ||
                path.contains("/media/display") ||
                path.contains("/newsletter/subscribe") ||
                path.contains("/newsletter/confirm");

        // 2. Public GET APIs (Public for reading, but restricted for writing)
        boolean isPublicGet = method == HttpMethod.GET && (
                path.contains("/posts/published") ||
                path.contains("/posts/slug") ||
                path.contains("/posts/category") ||
                path.contains("/taxonomy/categories") ||
                path.contains("/taxonomy/tags/trending")
        );

        if (isPublicPath || isPublicGet) {
            return chain.filter(exchange);
        }

        // 3. Authenticated APIs
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. Role-Based Access Control (RBAC)
        // Only Author, Premium, and Admin can create categories and tags
        if ((path.contains("/taxonomy/categories") || path.contains("/taxonomy/tags")) && (method == HttpMethod.POST || method == HttpMethod.DELETE)) {
            String role = jwtUtil.getRole(token);
            if (role == null || (!role.contains("AUTHOR") && !role.contains("PREMIUM") && !role.contains("ADMIN"))) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}