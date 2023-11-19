package com.malurus.postservice.integration.controller;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.malurus.postservice.integration.constants.GlobalConstants.*;
import static com.malurus.postservice.integration.constants.JsonConstants.REQUEST_PATTERN;
import static com.malurus.postservice.integration.constants.UrlConstants.POST_URL;
import static com.malurus.postservice.integration.constants.UrlConstants.POST_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(statements = "ALTER SEQUENCE posts_id_seq RESTART WITH 1;")
@SuppressWarnings("SameParameterValue")
class PostControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageSourceService;
    private final PostService postService;
    private final PostRepository postRepository;

    @Test
    void createPostTest() throws Exception {
        createPostAndExpectSuccess(DEFAULT_POST_TEXT.getConstant());
        createPostAndExpectFailure("");

        createQuotePostAndExpectSuccess(DEFAULT_POST_TEXT.getConstant(), 1L);
        createQuotePostAndExpectSuccess(DEFAULT_POST_TEXT.getConstant(), 1L);
        createQuotePostAndExpectFailure(
                DEFAULT_POST_TEXT.getConstant(),
                100L,
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        createQuotePostAndExpectFailure(
                "",
                1L,
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );

        assertTrue(postRepository.existsById(1L));
        assertTrue(postRepository.existsById(2L));
        assertTrue(postRepository.existsById(3L));
    }

    @Test
    void getPostTest() throws Exception {
        createDummyPost();

        getPostAndExpectSuccess(1L, 1);
        getPostAndExpectSuccess(1L, 1);
        getPostAndExpectFailure(100L);
    }

    @Test
    void updatePostTest() throws Exception {
        createDummyPost();

        updatePostAndExpectSuccess(1L, UPDATE_POST_TEXT.getConstant());
        updatePostAndExpectFailure(
                1L,
                "",
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
        updatePostAndExpectFailure(
                100L,
                UPDATE_POST_TEXT.getConstant(),
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
    }

    @Test
    void deletePostTest() throws Exception {
        createDummyPost();

        deletePost(100L, false);
        deletePost(1L, true);
    }

    private void createPostAndExpectSuccess(String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                POST_URL.getConstant())
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant()));

        expectOkPostResponse(resultActions, text, 0);
    }

    private void createPostAndExpectFailure(String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                POST_URL.getConstant())
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant()));

        expectFailResponse(resultActions, BAD_REQUEST, "$.text", TEXT_EMPTY_MESSAGE.getConstant());
    }

    private void createQuotePostAndExpectSuccess(String text, Long quoteToId) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                POST_URL_WITH_ID.getConstant().formatted(quoteToId))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant())
        );

        expectOkQuotePostResponse(resultActions, text, 0);
    }

    private void createQuotePostAndExpectFailure(String text, Long quoteToId, HttpStatus status, String jsonPath, String message) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                POST_URL_WITH_ID.getConstant().formatted(quoteToId))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant())
        );

        expectFailResponse(resultActions, status, jsonPath, message);
    }

    private void getPostAndExpectSuccess(Long id, int views) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(
                POST_URL_WITH_ID.getConstant().formatted(id))
                .header("loggedInUser", ID.getConstant()));

        expectOkPostResponse(resultActions, DEFAULT_POST_TEXT.getConstant(), views);
    }

    private void getPostAndExpectFailure(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(
                POST_URL_WITH_ID.getConstant().formatted(id))
                .header("loggedInUser", ID.getConstant()));

        expectFailResponse(resultActions, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", id));
    }

    private void updatePostAndExpectSuccess(Long id, String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch(
                POST_URL_WITH_ID.getConstant().formatted(id))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant()));

        expectOkPostResponse(resultActions, text, 0);
    }

    private void updatePostAndExpectFailure(Long id, String text, HttpStatus status, String jsonPath, String message) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch(
                POST_URL_WITH_ID.getConstant().formatted(id))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", ID.getConstant()));

        expectFailResponse(resultActions, status, jsonPath, message);
    }

    private void deletePost(Long id, Boolean value) throws Exception {
        mockMvc.perform(delete(
                        POST_URL_WITH_ID.getConstant().formatted(id))
                        .header("loggedInUser", ID.getConstant()))
                .andExpectAll(
                        status().isOk(),
                        content().string(value.toString())
                );

        assertFalse(postRepository.existsById(id));
    }

    private void expectOkPostResponse(ResultActions resultActions, String text, int views) throws Exception {
        resultActions
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.replyTo").value(IsNull.nullValue()),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo").value(IsNull.nullValue()),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.text").value(text),
                        jsonPath("$.replies").exists(),
                        jsonPath("$.reposts").exists(),
                        jsonPath("$.likes").exists(),
                        jsonPath("$.views").value(views),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void expectOkQuotePostResponse(ResultActions resultActions, String text, int views) throws Exception {
        resultActions
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.replyTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo").value(IsNull.nullValue()),
                        jsonPath("$.quoteTo.text").value(DEFAULT_POST_TEXT.getConstant()),
                        jsonPath("$.quoteTo.replies").exists(),
                        jsonPath("$.quoteTo.reposts").exists(),
                        jsonPath("$.quoteTo.likes").exists(),
                        jsonPath("$.quoteTo.views").exists(),
                        jsonPath("$.quoteTo.creationDate").exists(),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.text").value(text),
                        jsonPath("$.replies").exists(),
                        jsonPath("$.reposts").exists(),
                        jsonPath("$.likes").exists(),
                        jsonPath("$.views").value(views),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void expectFailResponse(ResultActions resultActions, HttpStatus status, String jsonPath, String message) throws Exception {
        resultActions
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void createDummyPost() {
        postService.createPost(new PostCreateRequest(DEFAULT_POST_TEXT.getConstant()), ID.getConstant());
    }
}
