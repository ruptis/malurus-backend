package com.malurus.userservice.controller;

import com.malurus.userservice.dto.request.CreateUserRequest;
import com.malurus.userservice.dto.request.UpdateUserRequest;
import com.malurus.userservice.dto.response.PageResponse;
import com.malurus.userservice.dto.response.UserResponse;
import com.malurus.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthUser(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(userService.getAuthUser(loggedInUser));
    }

    @GetMapping("/")
    public ResponseEntity<PageResponse<UserResponse>> findAllByUsername(
            @RequestParam String username,
            Pageable pageable
    ) {
        var response = PageResponse.of(userService.findAllByUsername(username, pageable));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(userService.updateUser(request, loggedInUser));
    }

    @GetMapping("/id/{email}")
    public ResponseEntity<String> getUserIdByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserIdByEmail(email));
    }
}
