package com.malurus.postservice.repository;

import com.malurus.postservice.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByParentPostIdAndProfileId(Long parentPostId, String profileId);

    Integer countAllByParentPostId(Long parentPostId);
}
