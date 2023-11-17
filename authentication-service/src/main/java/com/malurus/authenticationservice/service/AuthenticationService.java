package com.malurus.authenticationservice.service;

import com.malurus.authenticationservice.client.UserServiceClient;
import com.malurus.authenticationservice.client.request.CreateUserRequest;
import com.malurus.authenticationservice.dto.request.AuthenticationRequest;
import com.malurus.authenticationservice.dto.request.RegisterRequest;
import com.malurus.authenticationservice.dto.response.AuthenticationResponse;
import com.malurus.authenticationservice.dto.response.RegisterResponse;
import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.exception.InvalidCredentialsException;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final AccountService accountService;
    private final TokenService tokenService;
    private final MessageSourceService messageService;
    private final UserServiceClient userServiceClient;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        if (accountService.isAccountExists(request.email())) {
            throw new EntityExistsException(
                    messageService.generateMessage("error.account.already_exists", request.email())
            );
        }

        Account newAccount = accountService.createAccount(request.email(), request.password());
        log.info("account {} has been created", newAccount.getId());

        CreateUserRequest createProfileRequest = new CreateUserRequest(request.username(), request.email(), LocalDate.now());
        String userId = userServiceClient.createUser(createProfileRequest);
        newAccount.setUserId(userId);
        log.info("profile {} has been created", userId);

        return RegisterResponse.builder()
                .message(messageService.generateMessage("register.success"))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException(
                    messageService.generateMessage("error.account.credentials_invalid")
            );
        }

        Account account = accountService.findAccountByEmail(request.email());

        String jwt = jwtService.generateJwt(account);

        tokenService.deleteTokenByAccount(account);
        tokenService.createToken(account, jwt);
        log.info("jwt was generated {}", jwt);

        return AuthenticationResponse.builder()
                .jwt(jwt)
                .build();
    }
}
