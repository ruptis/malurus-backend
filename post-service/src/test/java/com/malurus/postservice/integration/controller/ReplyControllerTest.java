package com.malurus.postservice.integration.controller;

import com.malurus.postservice.dto.request.PostCreateRequest;
import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.service.MessageSourceService;
import com.malurus.postservice.service.ReplyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.malurus.postservice.integration.constants.GlobalConstants.*;
import static com.malurus.postservice.integration.constants.JsonConstants.REQUEST_PATTERN;
import static com.malurus.postservice.integration.constants.UrlConstants.REPLY_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:/sql/data.sql")
@SuppressWarnings("SameParameterValue")
class ReplyControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ReplyService replyService;
    private final MessageSourceService messageSourceService;

    @Test
    void replyTest() throws Exception {
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_POST_TEXT.getConstant(), 1, 1);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_POST_TEXT.getConstant(), 2, 2);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 3L, DEFAULT_REPLY_TEXT.getConstant(), 1, 3);

        replyAndExpectFailure(
                100L,
                DEFAULT_REPLY_TEXT.getConstant(),
                0,
                3,
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        replyAndExpectFailure(
                1L,
                "",
                2,
                3,
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
    }

    @Test
    void getReplyTest() throws Exception {
        replyDummyPost(new PostCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        getReplyAndExpectSuccess(2L, 1L, DEFAULT_REPLY_TEXT.getConstant(), 1, 1);
        getReplyAndExpectFailure(100L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 100));
    }

    @Test
    void updateReplyTest() throws Exception {
        replyDummyPost(new PostCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        updateReplyAndExpectSuccess(2L, UPDATE_REPLY_TEXT.getConstant(), 1);
        updateReplyAndExpectFailure(
                100L,
                UPDATE_REPLY_TEXT.getConstant(),
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        updateReplyAndExpectFailure(
                2L,
                "",
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
    }

    @Test
    void deleteReplyTest() throws Exception {
        replyDummyPost(new PostCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        deleteReplyAndExpectSuccess(2L);
        deleteReplyAndExpectFailure(2L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 2));
    }

    private void getReplyAndExpectSuccess(Long replyId, Long replyToId, String replyText, int repliesForPost, int repliesForUser) throws Exception {
        mockMvc.perform(get(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.replies").value(repliesForPost),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(replyText),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
        checkNumberOfReplies(replyToId, repliesForPost, repliesForUser);
    }

    private void getReplyAndExpectFailure(Long replyId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(get(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void deleteReplyAndExpectSuccess(Long replyId) throws Exception {
        mockMvc.perform(delete(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );
    }

    private void deleteReplyAndExpectFailure(Long replyId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(delete(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void updateReplyAndExpectSuccess(Long replyId, String updatedReplyText, int repliesForPost) throws Exception {
        mockMvc.perform(patch(REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .content(REQUEST_PATTERN.getConstant().formatted(updatedReplyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.replies").value(repliesForPost),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(updatedReplyText),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void updateReplyAndExpectFailure(Long replyId, String updatedReplyText, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(patch(REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .content(REQUEST_PATTERN.getConstant().formatted(updatedReplyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void replyAndExpectSuccess(String replyText, Long replyToId, String replyToText, int repliesForPost, int repliesForUser) throws Exception {
        mockMvc.perform(post(REPLY_URL_WITH_ID.getConstant().formatted(replyToId))
                        .content(REQUEST_PATTERN.getConstant().formatted(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.text").value(replyToText),
                        jsonPath("$.replyTo.replies").value(repliesForPost),
                        jsonPath("$.replyTo.reposts").exists(),
                        jsonPath("$.replyTo.likes").exists(),
                        jsonPath("$.replyTo.views").exists(),
                        jsonPath("$.replyTo.userId").exists(),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.repostTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(replyText),
                        jsonPath("$.replies").exists(),
                        jsonPath("$.reposts").exists(),
                        jsonPath("$.likes").exists(),
                        jsonPath("$.views").exists(),
                        jsonPath("$.userId").value(ID.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
        checkNumberOfReplies(replyToId, repliesForPost, repliesForUser);
    }

    private void replyAndExpectFailure(
            Long replyToId,
            String replyText,
            int repliesForPost,
            int repliesForUser,
            HttpStatus status,
            String jsonPath,
            String message
    ) throws Exception {
        mockMvc.perform(patch(REPLY_URL_WITH_ID.getConstant().formatted(replyToId))
                        .content(REQUEST_PATTERN.getConstant().formatted(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", ID.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
        checkNumberOfReplies(replyToId, repliesForPost, repliesForUser);
    }

    private void checkNumberOfReplies(long replyToId, int repliesForPost, int repliesForUser) {
        try {
            assertEquals(repliesForPost, replyService.getAllRepliesForPost(replyToId, ID.getConstant()).size());
        } catch (EntityNotFoundException ignored) {
        }
        assertEquals(repliesForUser, replyService.getAllRepliesForUser(ID.getConstant(), PageRequest.of(0, 20)).size());
    }

    private void replyDummyPost(PostCreateRequest request, Long replyToId) {
        replyService.reply(request, replyToId, ID.getConstant());
    }
}
