package com.malurus.postservice.integration.controller;

import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.RepostService;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.malurus.postservice.integration.constants.GlobalConstants.*;
import static com.malurus.postservice.integration.constants.UrlConstants.REPOST_URL_WITH_ID;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:sql/data.sql")
@SuppressWarnings("SameParameterValue")
class RepostControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final PostRepository postRepository;
    private final MessageSourceService messageSourceService;
    private final RepostService repostService;

    @Test
    void repostTest() throws Exception {
        repostAndExpectSuccess(1L);

        getRepostAndExpectSuccess(2L);
        repostAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
        repostAndExpectFailure(1L, BAD_REQUEST, ERROR_DUPLICATE_ENTITY.getConstant());
    }

    @Test
    void undoRepostTest() throws Exception {
        repostDummyPost();

        undoRepostAndExpectSuccess(1L);
        getRepostAndExpectFailure(2L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 2));
        undoRepostAndExpectFailure(1L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 1));
    }

    @Test
    void getRepostTest() throws Exception {
        repostDummyPost();

        getRepostAndExpectSuccess(2L);
        getRepostAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
    }

    private void repostAndExpectSuccess(Long repostToId) throws Exception {
        mockMvc.perform(post(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostToId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );

        assertTrue(postRepository.findByIdAndRepostToIsNotNull(repostToId+1).isPresent());
    }

    private void repostAndExpectFailure(Long repostToId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(post(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostToId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(containsStringIgnoringCase(message))
                );
    }

    private void undoRepostAndExpectSuccess(Long repostToId) throws Exception {
        mockMvc.perform(delete(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostToId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );

        assertFalse(postRepository.findByRepostToIdAndUserId(repostToId, ID.getConstant()).isPresent());
    }

    private void undoRepostAndExpectFailure(Long repostToId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(delete(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostToId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void getRepostAndExpectSuccess(Long repostId) throws Exception {
        mockMvc.perform(get(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.replyTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo.text").value(DEFAULT_POST_TEXT.getConstant()),
                        jsonPath("$.repostTo.userId").value(ID.getConstant()),
                        jsonPath("$.repostTo.replies").exists(),
                        jsonPath("$.repostTo.reposts").value(1),
                        jsonPath("$.repostTo.likes").exists(),
                        jsonPath("$.repostTo.views").exists(),
                        jsonPath("$.repostTo.creationDate").exists(),
                        jsonPath("$.repostTo.isReposted").value(Boolean.TRUE),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void getRepostAndExpectFailure(Long repostId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(get(
                        REPOST_URL_WITH_ID.getConstant().formatted(repostId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void repostDummyPost() {
        repostService.repost(1L, ID.getConstant());
    }
}
