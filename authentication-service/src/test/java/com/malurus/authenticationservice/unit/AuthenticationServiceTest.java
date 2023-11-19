package com.malurus.authenticationservice.unit;

import com.malurus.authenticationservice.client.UserServiceClient;
import com.malurus.authenticationservice.dto.request.AuthenticationRequest;
import com.malurus.authenticationservice.dto.request.RegisterRequest;
import com.malurus.authenticationservice.dto.response.AuthenticationResponse;
import com.malurus.authenticationservice.dto.response.RegisterResponse;
import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.exception.InvalidCredentialsException;
import com.malurus.authenticationservice.service.*;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AccountService accountService;

    @Mock
    private TokenService tokenService;

    @Mock
    private MessageSourceService messageService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_SuccessfulRegistration_ReturnsRegisterResponse() {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "username", "password");
        when(accountService.isAccountExists(registerRequest.email())).thenReturn(false);
        when(userServiceClient.createUser(any())).thenReturn("userId");
        when(accountService.createAccount(any(), any(), any())).thenReturn(Account.builder().id("userId").email("test@example.com").password("encodedPassword").build());

        RegisterResponse result = authenticationService.register(registerRequest);

        assertNotNull(result);
        verify(userServiceClient, times(1)).createUser(any());
        verify(accountService, times(1)).createAccount(any(), any(), any());
    }

    @Test
    void register_AccountAlreadyExists_ThrowsEntityExistsException() {
        RegisterRequest registerRequest = new RegisterRequest("username", "test@example.com", "password");
        when(accountService.isAccountExists(registerRequest.email())).thenReturn(true);
        when(messageService.generateMessage(any(), any())).thenReturn("Error Message");

        EntityExistsException exception = assertThrows(EntityExistsException.class, () -> authenticationService.register(registerRequest));
        assertEquals("Error Message", exception.getMessage());
        verify(userServiceClient, never()).createUser(any());
        verify(accountService, never()).createAccount(any(), any(), any());
        verifyNoMoreInteractions(userServiceClient, accountService);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsAuthenticationResponse() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", "password");
        Account testAccount = Account.builder().id("userId").email("test@example.com").password("encodedPassword").build();
        when(accountService.findAccountByEmail(authenticationRequest.email())).thenReturn(testAccount);
        when(jwtService.generateJwt(testAccount)).thenReturn("generatedJwt");
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("userId", "password"));

        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest);

        assertNotNull(result);
        assertEquals("generatedJwt", result.jwt());
        verify(jwtService, times(1)).generateJwt(testAccount);
        verify(tokenService, times(1)).deleteTokenByAccount(testAccount);
        verify(tokenService, times(1)).createToken(testAccount, "generatedJwt");
    }

    @Test
    void authenticate_InvalidCredentials_ThrowsInvalidCredentialsException() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", "wrongPassword");
        when(accountService.findAccountByEmail(authenticationRequest.email())).thenReturn(Account.builder().id("userId").email("test@example.com").password("encodedPassword").build());
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid Credentials"));
        when(messageService.generateMessage(any())).thenReturn("Error Message");

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(authenticationRequest));
        assertEquals("Error Message", exception.getMessage());
        verify(jwtService, never()).generateJwt(any());
        verify(tokenService, never()).deleteTokenByAccount(any());
        verify(tokenService, never()).createToken(any(), any());
        verifyNoMoreInteractions(jwtService, tokenService);
    }
}
