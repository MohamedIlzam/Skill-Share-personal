package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.user.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM UserProfile p WHERE " +
           "(:search IS NULL OR :search = '' " +
           "OR lower(p.fullName) LIKE lower(concat('%', :search, '%')) " +
           "OR lower(p.location) LIKE lower(concat('%', :search, '%')) " +
           "OR lower(p.university) LIKE lower(concat('%', :search, '%'))) " +
           "AND p.user.id != :currentUserId")
    Page<UserProfile> searchByKeywordExceptUser(@org.springframework.data.repository.query.Param("search") String search, 
                                                @org.springframework.data.repository.query.Param("currentUserId") Long currentUserId, 
                                                Pageable pageable);
}
