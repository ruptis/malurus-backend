package com.malurus.socialgraphservice.repository;

import com.malurus.socialgraphservice.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    Integer countAllByFollowerId(String followerId);
    Integer countAllByFolloweeId(String followeeId);
    List<Relationship> findAllByFollowerId(String followerId);
    List<Relationship> findAllByFolloweeId(String followeeId);
    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);
    Optional<Relationship> deleteByFollowerIdAndFolloweeId(String followerId, String followeeId);
}
