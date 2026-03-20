package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    // Fetch all skills for a specific user (Required for UI display)
    List<Skill> findAllByOwnerId(Long userId);
    
    // Secure fetch: Fetches a skill ONLY if the given user owns it
    Optional<Skill> findByIdAndOwnerId(Long id, Long ownerId);

    // Feature 3: Browse all skills NOT owned by the current user
    @EntityGraph(attributePaths = {"owner"})
    List<Skill> findByOwnerIdNot(Long ownerId);

    // Feature 3: Search skills by name (case-insensitive) NOT owned by the current user
    @EntityGraph(attributePaths = {"owner"})
    List<Skill> findByNameContainingIgnoreCaseAndOwnerIdNot(String name, Long ownerId);
}
