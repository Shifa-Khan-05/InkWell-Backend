package com.authservice.config;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    @Lazy // ✅ This breaks the circular dependency cycle
    private AuthService authService;

    @Value("${frontend.url:https://inkwell-blogging.netlify.app}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Process logic via AuthService
        String token = authService.processOAuthPostLogin(email, name, "google");
        String role = authService.getRoleByEmail(email);
        
        // Fetch the actual userId from your database using the email
        Integer userId = authService.getUserIdByEmail(email); 

        // Redirect to frontend with query params for OAuthSuccess.jsx
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-success")
                .queryParam("token", token)
                .queryParam("role", role)
                .queryParam("userId", userId) // CRITICAL: Now the frontend can see the ID
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}