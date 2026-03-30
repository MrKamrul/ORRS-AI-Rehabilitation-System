package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
