package com.malurus.authenticationservice.service;

import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.malurus.authenticationservice.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceService messageService;

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.generateMessage("error.entity.not_found", email)
                ));
    }

    public Account findAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.generateMessage("error.entity.not_found", id)
                ));
    }

    public Account createAccount(String id, String email, String password) {
        return accountRepository.saveAndFlush(
                Account.builder()
                        .id(id)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .isAccountNonLocked(true)
                        .isAccountNonExpired(true)
                        .isCredentialsNonExpired(true)
                        .isEnabled(true)
                        .role(USER)
                        .build()
        );
    }

    public boolean isAccountExists(String email) {
        return accountRepository.findByEmail(email)
                .isPresent();
    }
}
