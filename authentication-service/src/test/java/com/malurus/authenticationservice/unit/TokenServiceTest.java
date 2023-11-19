package com.malurus.authenticationservice.unit;

import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.entity.Token;
import com.malurus.authenticationservice.exception.InvalidTokenException;
import com.malurus.authenticationservice.model.TokenType;
import com.malurus.authenticationservice.repository.TokenRepository;
import com.malurus.authenticationservice.service.JwtService;
import com.malurus.authenticationservice.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void createToken_ValidAccount_CallsRepositorySave() {
        Account account = new Account();
        String jwt = "mockJwt";

        // Call createToken
        tokenService.createToken(account, jwt);

        // Verify that tokenRepository.save was called with the expected Token
        verify(tokenRepository).save(argThat(token ->
                token.getAccount().equals(account) &&
                        token.getJwt().equals(jwt) &&
                        token.getTokenType().equals(TokenType.BEARER) &&
                        !token.isExpired() &&
                        !token.isRevoked()
        ));
    }

    @Test
    void deleteTokenByAccount_ExistingAccount_CallsRepositoryDelete() {
        Account account = new Account();
        when(tokenRepository.findByAccount_Id(account.getId())).thenReturn(Optional.of(mock(Token.class)));

        // Call deleteTokenByAccount
        tokenService.deleteTokenByAccount(account);

        // Verify that tokenRepository.delete was called
        verify(tokenRepository).delete(any());
    }

    @Test
    void deleteTokenByAccount_NonexistentAccount_DoesNothing() {
        Account account = new Account();
        when(tokenRepository.findByAccount_Id(account.getId())).thenReturn(Optional.empty());

        // Call deleteTokenByAccount
        tokenService.deleteTokenByAccount(account);

        // Verify that tokenRepository.delete was not called
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void isTokenValid_InvalidToken_ThrowsException() {
        String jwt = "mockInvalidJwt";

        when(jwtService.extractId(jwt)).thenReturn("mockUserId");
        when(tokenRepository.findByJwt(jwt)).thenReturn(Optional.empty());

        // Call isTokenValid and verify that it throws an InvalidTokenException
        assertThrows(InvalidTokenException.class, () -> tokenService.isTokenValid(jwt));
    }
}
