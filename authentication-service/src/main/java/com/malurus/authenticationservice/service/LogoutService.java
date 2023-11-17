package com.malurus.authenticationservice.service;

import com.malurus.authenticationservice.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenService tokenService;
    private final AccountService accountService;
    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String jwt = jwtService.extractJwt(request);

        if (jwt != null && !jwt.isEmpty()) {
            String userId = jwtService.extractId(jwt);
            Account account = accountService.findAccountById(userId);

            tokenService.deleteTokenByAccount(account);
            SecurityContextHolder.clearContext();
            log.info("user with id {} has logged out", userId);
        }
    }
}
