package com.malurus.authenticationservice.controller;

import com.malurus.authenticationservice.dto.request.AuthenticationRequest;
import com.malurus.authenticationservice.dto.request.RegisterRequest;
import com.malurus.authenticationservice.dto.response.AuthenticationResponse;
import com.malurus.authenticationservice.dto.response.RegisterResponse;
import com.malurus.authenticationservice.service.AuthenticationService;
import com.malurus.authenticationservice.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/validate/{jwt}")
    public ResponseEntity<String> isTokenValid(@PathVariable String jwt) {
        return ResponseEntity.ok(tokenService.isTokenValid(jwt));
    }
}
