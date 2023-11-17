package com.malurus.postservice.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "posts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"repost_to_id", "profileId"}),
        indexes = {
                @Index(columnList = "reply_to_id", name = "reply_to_id"),
                @Index(columnList = "quote_to_id", name = "quote_to_id"),
                @Index(columnList = "repost_to_id", name = "repost_to_id")
        }
)
public class Post implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String text;
    private String profileId;
    private LocalDateTime creationDate;
    @ElementCollection
    private Set<String> mediaUrls = new HashSet<>();

    @OneToMany(
            targetEntity = Like.class,
            mappedBy = "parentPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Like> likes = new HashSet<>();

    @OneToMany(
            targetEntity = Post.class,
            mappedBy = "repostTo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Post> reposts = new HashSet<>();

    @OneToMany(
            targetEntity = Post.class,
            mappedBy = "replyTo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Post> replies = new HashSet<>();

    @OneToMany(
            targetEntity = View.class,
            mappedBy = "parentPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<View> views = new HashSet<>();

    @ManyToOne(targetEntity = Post.class)
    @Nullable
    private Post repostTo;

    @ManyToOne(targetEntity = Post.class)
    @Nullable
    private Post replyTo;

    @ManyToOne(targetEntity = Post.class)
    @Nullable
    private Post quoteTo;
}
