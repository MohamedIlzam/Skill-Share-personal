package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long>, JpaSpecificationExecutor<Skill> {
    
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

    // Feature 5: Dynamic Filtering with Specification and EntityGraph eager owner loading
    @EntityGraph(attributePaths = {"owner"})
    List<Skill> findAll(@Nullable Specification<Skill> spec, Sort sort);
}
