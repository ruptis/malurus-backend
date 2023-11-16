package com.malurus.authenticationservice.mocks;

import com.malurus.authenticationservice.client.UserServiceClient;
import com.malurus.authenticationservice.client.request.CreateUserRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserClientMock {
    public static void setupMockUserResponse(UserServiceClient profileServiceClient) {
        when(profileServiceClient.createUser(any(CreateUserRequest.class)))
                .thenReturn("dummy-profile-id");
    }
}
