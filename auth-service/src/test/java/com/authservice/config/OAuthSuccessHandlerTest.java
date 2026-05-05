package com.authservice.config;

import com.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthSuccessHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuthSuccessHandler oauthSuccessHandler;

    @Test
    void onAuthenticationSuccess_RedirectsToFrontend() throws IOException {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(authService.processOAuthPostLogin(anyString(), anyString(), anyString())).thenReturn("token");
        when(authService.getRoleByEmail("test@test.com")).thenReturn("ROLE_READER");
        when(authService.getUserIdByEmail("test@test.com")).thenReturn(1);

        oauthSuccessHandler.setRedirectStrategy(redirectStrategy);
        oauthSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=token"));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("role=ROLE_READER"));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("userId=1"));
    }
}
