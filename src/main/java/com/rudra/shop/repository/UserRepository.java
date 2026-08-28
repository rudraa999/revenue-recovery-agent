package com.rudra.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rudra.shop.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);
}
