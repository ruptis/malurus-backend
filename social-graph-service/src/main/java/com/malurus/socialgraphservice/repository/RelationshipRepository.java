package com.malurus.socialgraphservice.repository;

import com.malurus.socialgraphservice.entity.Relationship;
import com.malurus.socialgraphservice.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    List<Relationship> findAllByFollowerId(String followerId);
    List<Relationship> findAllByFolloweeId(String followeeId);

    boolean existsRelationshipByFollowerIdAndFolloweeId(String followerId, String followeeId);
    boolean existsRelationshipByFollowerAndFollowee(User follower, User followee);

    @Transactional
    void deleteRelationshipByFollowerAndFollowee(User follower, User followee);
}
