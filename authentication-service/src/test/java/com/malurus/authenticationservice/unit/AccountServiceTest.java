package com.malurus.authenticationservice.unit;

import com.malurus.authenticationservice.entity.Account;
import com.malurus.authenticationservice.repository.AccountRepository;
import com.malurus.authenticationservice.service.AccountService;
import com.malurus.authenticationservice.service.MessageSourceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSourceService messageSourceService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testFindAccountByEmail_ExistingEmail_ReturnsAccount() {
        String email = "test@example.com";
        Account mockAccount = new Account();
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(mockAccount));

        Account result = accountService.findAccountByEmail(email);

        assertNotNull(result);
        assertEquals(mockAccount, result);
    }

    @Test
    void testFindAccountByEmail_NonExistingEmail_ThrowsEntityNotFoundException() {
        String email = "nonexistent@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(any(), any())).thenReturn("Error Message");

        assertThrows(EntityNotFoundException.class, () -> accountService.findAccountByEmail(email));
    }

    @Test
    void testFindAccountById_ExistingId_ReturnsAccount() {
        String accountId = "123";
        Account mockAccount = new Account();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        Account result = accountService.findAccountById(accountId);

        assertNotNull(result);
        assertEquals(mockAccount, result);
    }

    @Test
    void testFindAccountById_NonExistingId_ThrowsEntityNotFoundException() {
        String accountId = "nonexistentId";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        when(messageSourceService.generateMessage(any(), any())).thenReturn("Error Message");

        assertThrows(EntityNotFoundException.class, () -> accountService.findAccountById(accountId));
    }

    @Test
    void testCreateAccount_SuccessfullyCreatesAccount() {
        String accountId = "123";
        String email = "test@example.com";
        String password = "password";
        when(accountRepository.saveAndFlush(any())).thenReturn(new Account());

        Account result = accountService.createAccount(accountId, email, password);

        assertNotNull(result);
        verify(accountRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void testIsAccountExists_ExistingEmail_ReturnsTrue() {
        String email = "test@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(new Account()));

        boolean result = accountService.isAccountExists(email);

        assertTrue(result);
    }

    @Test
    void testIsAccountExists_NonExistingEmail_ReturnsFalse() {
        String email = "nonexistent@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = accountService.isAccountExists(email);

        assertFalse(result);
    }
}
