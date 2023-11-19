package com.malurus.authenticationservice.unit;

import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.service.AccountService;
import com.malurus.authenticationservice.service.JwtService;
import com.malurus.authenticationservice.service.LogoutService;
import com.malurus.authenticationservice.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private AccountService accountService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void logout_ValidJwt_CallsTokenServiceAndLogs() {
        // Mock JWT extraction
        when(jwtService.extractJwt(request)).thenReturn("mockJwt");

        // Mock JWT extraction
        when(jwtService.extractId("mockJwt")).thenReturn("mockUserId");

        // Mock finding account
        when(accountService.findAccountById("mockUserId")).thenReturn(mock(Account.class));

        // Call logout
        logoutService.logout(request, response, authentication);

        // Verify that tokenService.deleteTokenByAccount and log.info were called
        verify(tokenService).deleteTokenByAccount(any());

        // Verify that SecurityContextHolder.clearContext was called
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_NoJwt_DoesNothing() {
        // Mock JWT extraction
        when(jwtService.extractJwt(request)).thenReturn(null);

        // Call logout
        logoutService.logout(request, response, authentication);

        // Verify that tokenService.deleteTokenByAccount and log.info were not called
        verify(tokenService, never()).deleteTokenByAccount(any());

        // Verify that SecurityContextHolder.clearContext was not called
        SecurityContextHolder.clearContext();
    }
}