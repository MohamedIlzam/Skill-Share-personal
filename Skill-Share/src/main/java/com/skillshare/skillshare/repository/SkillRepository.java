package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    // Fetch all skills for a specific user (Required for UI display)
    List<Skill> findAllByOwnerId(Long userId);
    
    // Secure fetch: Fetches a skill ONLY if the given user owns it
    Optional<Skill> findByIdAndOwnerId(Long id, Long ownerId);
}
