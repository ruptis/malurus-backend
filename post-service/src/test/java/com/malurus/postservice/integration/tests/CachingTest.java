package com.malurus.postservice.integration.tests;

import com.malurus.postservice.constant.EntityName;
import com.malurus.postservice.constant.Operation;
import com.malurus.postservice.dto.request.PostUpdateRequest;
import com.malurus.postservice.dto.response.PostResponse;
import com.malurus.postservice.entity.Post;
import com.malurus.postservice.integration.IntegrationTestBase;
import com.malurus.postservice.repository.PostRepository;
import com.malurus.postservice.service.PostService;
import com.malurus.postservice.service.ReplyService;
import com.malurus.postservice.service.RepostService;
import com.malurus.postservice.service.ViewService;
import com.malurus.postservice.util.PostUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.malurus.postservice.constant.CacheName.*;
import static com.malurus.postservice.integration.constants.GlobalConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
@Sql(statements = "ALTER SEQUENCE posts_id_seq RESTART WITH 1;")
@SuppressWarnings("SameParameterValue")
class CachingTest extends IntegrationTestBase {

    private final CacheManager cacheManager;
    private final PostService postService;
    private final RepostService repostService;
    private final ReplyService replyService;

    @MockBean
    private final PostRepository postRepository;

    @SpyBean
    private final PostUtil postUtil;

    @MockBean
    private final ViewService viewService;

    @Test
    void cachePostTest() {
        Post post = createStubForPost(1L, false);

        postService.getPostById(post.getId(), ID.getConstant());
        PostResponse postResponse = postService.getPostById(post.getId(), ID.getConstant());

        verify(postRepository, times(1)).findById(post.getId());

        PostResponse postFromCache = getEntityFromCache(post.getId(), POSTS_CACHE_NAME);
        assertNotNull(postFromCache);
        assertEquals(postResponse, postFromCache);
    }

    @Test
    void updatePostInCacheTest() {
        Post post = createStubForPost(1L, false);
        Post repost = createStubForRepost(post, 2L);
        Post reply = createStubForReply(post, 3L);

        postService.getPostById(post.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(post.getId(), POSTS_CACHE_NAME));

        PostResponse postResponse = postService.updatePost(
                post.getId(),
                new PostUpdateRequest(UPDATE_POST_TEXT.getConstant()),
                ID.getConstant()
        );
        PostResponse updatedPostFromCache = getEntityFromCache(post.getId(), POSTS_CACHE_NAME);
        assertNotNull(updatedPostFromCache);
        assertEquals(postResponse, updatedPostFromCache);

        repostService.getRepostById(repost.getId(), ID.getConstant());
        PostResponse repostFromCache = getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME);
        assertNotNull(repostFromCache);
        assertEquals(updatedPostFromCache, repostFromCache.getRepostTo());

        replyService.getReplyById(reply.getId(), ID.getConstant());
        PostResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(updatedPostFromCache, replyFromCache.getReplyTo());
    }

    @Test
    void deletePostFromCacheTest() {
        Post post = createStubForPost(1L, true);
        Post repost = createStubForRepost(post, 2L);
        Post reply = createStubForReply(post, 3L);

        postService.getPostById(post.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(post.getId(), POSTS_CACHE_NAME));

        repostService.getRepostById(repost.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME));

        replyService.getReplyById(reply.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));

        postService.deletePost(post.getId(), ID.getConstant());
        assertNull(getEntityFromCache(post.getId(), POSTS_CACHE_NAME));
        assertNull(getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME));
        assertNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));
    }

    @Test
    void cacheRepliesForPostTest() {
        Post post = createStubForPostWithReplies(1L, 100);

        replyService.getAllRepliesForPost(post.getId(), ID.getConstant());
        List<PostResponse> repliesFromCache = getEntitiesFromCache(post.getId(), REPLIES_FOR_POST_CACHE_NAME);
        assertNotNull(repliesFromCache);
        assertEquals(100, repliesFromCache.size());
    }

    @Test
    void cacheReplyTest() {
        Post post = createStubForPost(1L, false);
        Post reply = createStubForReply(post, 2L);

        PostResponse replyResponse = replyService.getReplyById(reply.getId(), ID.getConstant());
        PostResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(replyResponse, replyFromCache);
    }

    @Test
    void updateReplyInCacheTest() {
        Post post = createStubForPost(1L, false);
        Post reply = createStubForReply(post, 2L);

        replyService.getReplyById(reply.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));

        PostResponse replyResponse = replyService.updateReply(
                reply.getId(),
                new PostUpdateRequest(UPDATE_REPLY_TEXT.getConstant()),
                ID.getConstant()
        );
        PostResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(replyResponse, replyFromCache);
    }

    @Test
    void deleteReplyFromCacheTest() {
        Post post = createStubForPostWithReplies(1L, 100);
        Post reply = createStubForReply(post, 2L);
        replyService.getAllRepliesForPost(post.getId(), ID.getConstant());
        replyService.getReplyById(reply.getId(), ID.getConstant());

        replyService.deleteReply(reply.getId(), ID.getConstant());
        assertNull(getEntitiesFromCache(post.getId(), REPLIES_FOR_POST_CACHE_NAME));
        assertNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));
        assertNull(getEntityFromCache(post.getId(), POSTS_CACHE_NAME));
    }

    @Test
    void cacheRepostTest() {
        Post post = createStubForPost(1L, true);
        Post repost = createStubForRepost(post, 2L);

        repostService.getRepostById(repost.getId(), ID.getConstant());
        PostResponse repostResponse = repostService.getRepostById(repost.getId(), ID.getConstant());

        verify(postRepository, times(1)).findByIdAndRepostToIsNotNull(repost.getId());

        PostResponse repostFromCache = getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME);
        assertNotNull(repostFromCache);
        assertEquals(repostResponse, repostFromCache);
    }

    @Test
    void deleteRepostFromCache() {
        Post post = createStubForPost(1L, true);
        Post repost = createStubForRepost(post, 2L);

        repostService.getRepostById(repost.getId(), ID.getConstant());
        assertNotNull(getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME));

        repostService.undoRepost(post.getId(), ID.getConstant());
        assertNull(getEntityFromCache(repost.getId(), REPOSTS_CACHE_NAME));
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private Post createStubForPostWithReplies(long postId, int replies) {
        Post parentPost = createStubForPost(postId, false);

        List<Post> repliesForPostFromDb = mock(ArrayList.class);
        List<PostResponse> repliesForPost = mock(ArrayList.class);
        Stream<Post> mockStreamOfReplies = mock(Stream.class);

        when(postRepository.findAllByReplyToIdOrderByCreationDateDesc(postId))
                .thenReturn(repliesForPostFromDb);

        when(repliesForPostFromDb.stream())
                .thenReturn(mockStreamOfReplies);

        when(mockStreamOfReplies.map(any(Function.class)))
                .thenReturn(mockStreamOfReplies);

        when(mockStreamOfReplies.collect(any()))
                .thenReturn(repliesForPost);

        when(repliesForPost.size())
                .thenReturn(replies);

        return parentPost;
    }

    private Post createStubForReply(Post replyTo, long replyId) {
        Post reply = buildDefaultReply(replyId, replyTo);
        replyTo.getReplies().add(reply);

        when(postRepository.findByIdAndReplyToIsNotNull(replyId))
                .thenReturn(Optional.of(reply));

        when(viewService.createViewEntity(eq(reply), anyString()))
                .thenReturn(reply);

        mockBasicThings(reply, replyId, false);

        return reply;
    }

    private Post createStubForRepost(Post repostTo, long repostId) {
        Post repost = buildDefaultRepost(repostId, repostTo);
        repostTo.getReposts().add(repost);

        when(postRepository.findByIdAndRepostToIsNotNull(repostId))
                .thenReturn(Optional.of(repost));

        when(postRepository.findByRepostToIdAndUserId(repostTo.getId(), ID.getConstant()))
                .thenReturn(Optional.of(repost));

        doNothing()
                .when(postRepository)
                .delete(repost);

        return repost;
    }

    private Post createStubForPost(long postId, boolean isReposted) {
        Post post = buildDefaultPost(postId);

        when(viewService.createViewEntity(eq(post), anyString()))
                .thenReturn(post);

        mockBasicThings(post, postId, isReposted);

        return post;
    }

    private void mockBasicThings(Post entity, long entityId, boolean isReposted) {
        when(postRepository.findById(entityId))
                .thenReturn(Optional.of(entity));

        when(postRepository.saveAndFlush(entity))
                .thenReturn(entity);

        doReturn(isReposted)
                .when(postUtil).isPostRepostedByUser(eq(entityId), anyString());

        doReturn(false)
                .when(postUtil).isPostLikedByUser(eq(entityId), anyString());

        doNothing()
                .when(postUtil).sendMessageToKafka(anyString(), any(Post.class), any(EntityName.class), any(Operation.class));

        when(postRepository.findAllByQuoteToId(entityId))
                .thenReturn(Collections.emptyList());
    }

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    private PostResponse getEntityFromCache(long entityId, String cacheName) {
        return cacheManager.getCache(cacheName).get(Long.toString(entityId), PostResponse.class);
    }

    @Nullable
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private List<PostResponse> getEntitiesFromCache(long parentEntityId, String cacheName) {
        return cacheManager.getCache(cacheName).get(Long.toString(parentEntityId), List.class);
    }

    private Post buildDefaultReply(long replyId, Post replyTo) {
        return Post.builder()
                .id(replyId)
                .text(DEFAULT_REPLY_TEXT.getConstant())
                .replyTo(replyTo)
                .creationDate(LocalDateTime.MAX)
                .userId(ID.getConstant())
                .reposts(new HashSet<>())
                .replies(new HashSet<>())
                .build();
    }

    private Post buildDefaultRepost(long repostId, Post repostTo) {
        return Post.builder()
                .id(repostId)
                .repostTo(repostTo)
                .creationDate(LocalDateTime.MAX)
                .userId(ID.getConstant())
                .build();
    }

    private Post buildDefaultPost(long postId) {
        return Post.builder()
                .id(postId)
                .text(DEFAULT_POST_TEXT.getConstant())
                .creationDate(LocalDateTime.MAX)
                .userId(ID.getConstant())
                .reposts(new HashSet<>())
                .replies(new HashSet<>())
                .build();
    }
}
