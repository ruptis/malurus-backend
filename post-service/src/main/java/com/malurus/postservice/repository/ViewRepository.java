package com.malurus.postservice.repository;

import com.malurus.postservice.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

    Optional<View> findByProfileIdAndParentPostId(String profileId, Long parentPostId);

    Integer countAllByParentPostId(Long parentPostId);
}
