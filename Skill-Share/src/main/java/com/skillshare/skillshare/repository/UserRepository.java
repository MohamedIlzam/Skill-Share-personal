package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.profile IS NULL")
    java.util.List<User> findAllUsersWithoutProfile();

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<User> searchByNameOrEmail(@org.springframework.data.repository.query.Param("query") String query);
}
