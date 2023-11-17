package com.malurus.postservice.repository;

import com.malurus.postservice.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByProfileIdAndReplyToIsNullAndRepostToIsNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Post> findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Post> findAllByProfileIdAndRepostToIsNotNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Post> findAllByReplyToIdOrderByCreationDateDesc(Long replyToId);

    List<Post> findAllByQuoteToId(Long quoteToId);

    Optional<Post> findByIdAndRepostToIsNotNull(Long rePostId);

    Optional<Post> findByIdAndReplyToIsNotNull(Long replyId);

    Optional<Post> findByRepostToIdAndProfileId(Long rePostToId, String profileId);

    Integer countAllByReplyToId(Long replyToId);

    Integer countAllByRepostToId(Long rePostToId);
}
