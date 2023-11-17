package com.malurus.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_post_id", "profileId"}),
        indexes = {
                @Index(columnList = "parent_post_id")
        }
)
public class View implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String profileId;

    @ManyToOne(targetEntity = Post.class)
    private Post parentPost;
}
