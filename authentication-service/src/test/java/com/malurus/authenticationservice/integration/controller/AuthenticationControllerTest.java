package com.malurus.authenticationservice.integration.controller;

import com.malurus.authenticationservice.client.UserServiceClient;
import com.malurus.authenticationservice.mocks.UserClientMock;
import com.malurus.authenticationservice.service.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.malurus.authenticationservice.integration.constants.AuthConstants.EXISTENT_ACCOUNT_EMAIL;
import static com.malurus.authenticationservice.integration.constants.AuthConstants.NEW_ACCOUNT_EMAIL;
import static com.malurus.authenticationservice.integration.constants.JsonConstants.EXISTENT_ACCOUNT_JSON;
import static com.malurus.authenticationservice.integration.constants.JsonConstants.NEW_ACCOUNT_JSON;
import static com.malurus.authenticationservice.integration.constants.UrlConstants.AUTHENTICATE_URL;
import static com.malurus.authenticationservice.integration.constants.UrlConstants.REGISTER_URL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class AuthenticationControllerTest {

    private final MockMvc mockMvc;
    private final MessageSourceService messageService;

    @MockBean
    private final UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        UserClientMock.setupMockUserResponse(userServiceClient);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        registerAccount(NEW_ACCOUNT_JSON.getConstant());
        authenticateUnactivatedAccountAndExpectForbidden(NEW_ACCOUNT_JSON.getConstant(), NEW_ACCOUNT_EMAIL.getConstant());
        String token = authenticateAccountAndExpectToken(NEW_ACCOUNT_JSON.getConstant());
        assertNotNull(token);
    }

    @Test
    void testRegisterFailure() throws Exception {
        registerExistingAccountAndExpectFailure(EXISTENT_ACCOUNT_JSON.getConstant(), EXISTENT_ACCOUNT_EMAIL.getConstant());
    }

    private void registerAccount(String account) throws Exception {
        mockMvc.perform(post(REGISTER_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value(messageService.generateMessage("activation.send.success"))
                );
    }

    private void registerExistingAccountAndExpectFailure(String account, String email) throws Exception {
        mockMvc.perform(post(REGISTER_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message").value(messageService.generateMessage("error.account.already_exists", email))
                );
    }

    private String authenticateAccountAndExpectToken(String account) throws Exception {
        ResultActions result = mockMvc.perform(post(AUTHENTICATE_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.jwt").exists()
                );

        return extractTokenFromResponse(result);
    }

    private void authenticateUnactivatedAccountAndExpectForbidden(String account, String email) throws Exception {
        mockMvc.perform(post(AUTHENTICATE_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.message").value(messageService.generateMessage("error.account.not_activated", email))
                );
    }

    private String extractTokenFromResponse(ResultActions resultActions) throws Exception {
        return new ObjectMapper()
                .readTree(
                        resultActions.andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .at("/jwt")
                .asText();
    }
}
