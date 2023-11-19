package com.malurus.postservice.integration.controller;


import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.repository.LikeRepository;
import com.malurus.postservice.service.LikeService;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.malurus.postservice.integration.constants.GlobalConstants.*;
import static com.malurus.postservice.integration.constants.UrlConstants.LIKE_URL_WITH_ID;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:sql/data.sql")
@SuppressWarnings("SameParameterValue")
class LikeControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageSourceService;
    private final LikeRepository likeRepository;
    private final PostService postService;
    private final LikeService likeService;

    @Test
    void likePostTest() throws Exception {
        likePostAndExpectSuccess(1L);

        likePostAndExpectFailure(100L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 100));
        likePostAndExpectFailure(1L, BAD_REQUEST, "$.message", ERROR_DUPLICATE_ENTITY.getConstant());
    }

    @Test
    void unlikePostTest() throws Exception {
        likeService.likePost(1L, ID.getConstant());
        unlikePostAndExpectSuccess(1L);

        unlikePostAndExpectFailure(100L);
        unlikePostAndExpectFailure(1L);
    }


    private void likePostAndExpectSuccess(Long postId) throws Exception {
        mockMvc.perform(post(
                        LIKE_URL_WITH_ID.getConstant().formatted(postId))
                        .header("loggedInUser", ID.getConstant()))
                .andExpectAll(
                        status().isCreated()
                );

        assertTrue(likeRepository.existsById(1L));
        assertTrue(postService.getPostById(postId, ID.getConstant()).getIsLiked());
    }

    private void likePostAndExpectFailure(Long postId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(post(
                        LIKE_URL_WITH_ID.getConstant().formatted(postId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(containsStringIgnoringCase(message))
                );
    }

    private void unlikePostAndExpectSuccess(Long postId) throws Exception {
        mockMvc.perform(delete(
                        LIKE_URL_WITH_ID.getConstant().formatted(postId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("")
                );

        assertFalse(likeRepository.existsById(1L));
        assertFalse(postService.getPostById(postId, ID.getConstant()).getIsLiked());
    }

    private void unlikePostAndExpectFailure(Long postId) throws Exception {
        mockMvc.perform(delete(
                        LIKE_URL_WITH_ID.getConstant().formatted(postId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message").value(messageSourceService.generateMessage("error.entity.not_found", postId))
                );
    }
}
