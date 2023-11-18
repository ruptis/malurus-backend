package com.malurus.socialgraphservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relationship", indexes = {
        @Index(name = "followee_idx", columnList = "followee"),
        @Index(name = "follower_followee_idx", columnList = "follower, followee", unique = true)
})
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower", nullable = false)
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followee", nullable = false)
    private User followee;

    @CreationTimestamp
    private LocalDateTime followDateTime;
}

