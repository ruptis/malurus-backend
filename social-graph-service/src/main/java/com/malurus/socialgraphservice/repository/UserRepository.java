package com.malurus.socialgraphservice.repository;

import com.malurus.socialgraphservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
