package com.malurus.authenticationservice.service;

import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.entity.Token;
import com.malurus.authenticationservice.exception.InvalidTokenException;
import com.malurus.authenticationservice.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import static com.malurus.authenticationservice.model.TokenType.BEARER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public void createToken(Account account, String jwt) {
        tokenRepository.save(
                Token.builder()
                        .account(account)
                        .jwt(jwt)
                        .tokenType(BEARER)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    public void deleteTokenByAccount(Account account) {
        tokenRepository.findByAccount_Id(account.getId())
                .ifPresent(tokenRepository::delete);
    }

    public String isTokenValid(String jwt) {
        UserDetails userDetails = extractUserDetails(jwt);
        boolean isTokenValid = tokenRepository.findByJwt(jwt)
                .map(token -> !token.isExpired() && !token.isRevoked())
                .orElse(false);

        if (isTokenValid && jwtService.isJwtValid(jwt, userDetails)) {
            log.info(userDetails.getUsername());
            return userDetails.getUsername();
        } else {
            throw new InvalidTokenException("Authentication token is invalid!");
        }
    }

    private UserDetails extractUserDetails(String jwt) {
        String userId = jwtService.extractId(jwt);
        return userDetailsService.loadUserByUsername(userId);
    }
}
