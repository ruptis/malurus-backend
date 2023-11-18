package com.malurus.socialgraphservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class User {
    @Id
    private String id;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL)
    private Set<Relationship> followersRelationships;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private Set<Relationship> followeesRelationships;

    public boolean isCelebrity() {
        return followersRelationships.size() > 10000;
    }
}
