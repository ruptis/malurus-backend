package com.malurus.postservice.repository;

import com.malurus.postservice.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByUserIdAndReplyToIsNullAndRepostToIsNullOrderByCreationDateDesc(String userId, Pageable page);

    List<Post> findAllByUserIdAndReplyToIsNotNullOrderByCreationDateDesc(String userId, Pageable page);

    List<Post> findAllByUserIdAndRepostToIsNotNullOrderByCreationDateDesc(String userId, Pageable page);

    List<Post> findAllByReplyToIdOrderByCreationDateDesc(Long replyToId);

    List<Post> findAllByQuoteToId(Long quoteToId);

    Optional<Post> findByIdAndRepostToIsNotNull(Long repostId);

    Optional<Post> findByIdAndReplyToIsNotNull(Long replyId);

    Optional<Post> findByRepostToIdAndUserId(Long repostToId, String userId);

    Integer countAllByReplyToId(Long replyToId);

    Integer countAllByRepostToId(Long repostToId);
}
