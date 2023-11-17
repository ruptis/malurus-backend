package com.malurus.socialgraphservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relationship", indexes = {
        @Index(name = "follower_idx", columnList = "followerId"),
        @Index(name = "followee_idx", columnList = "followeeId")
})
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String followerId;

    private String followeeId;

    private LocalDateTime followDateTime;
}
