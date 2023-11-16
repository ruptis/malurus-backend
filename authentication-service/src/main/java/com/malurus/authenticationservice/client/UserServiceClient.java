package com.malurus.authenticationservice.client;

import com.malurus.authenticationservice.client.request.CreateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${services.user.name}")
public interface UserServiceClient {

    @PostMapping(value = "/api/v1/users")
    String createUser(@RequestBody CreateUserRequest request);
}
