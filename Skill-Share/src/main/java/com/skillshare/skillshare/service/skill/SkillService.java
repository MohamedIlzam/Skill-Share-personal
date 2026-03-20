package com.skillshare.skillshare.service.skill;

import com.skillshare.skillshare.dto.skill.SkillRequest;
import com.skillshare.skillshare.model.skill.Skill;

import java.util.List;

public interface SkillService {

    // --- Create ---
    Skill addSkill(Long userId, SkillRequest request);

    // --- Read ---
    List<Skill> getSkillsByUser(Long userId);
    
    Skill getSkillByIdForUser(Long userId, Long skillId);

    // --- Browse / Search ---
    List<Skill> getAvailableSkills(Long currentUserId);
    List<Skill> searchAvailableSkills(String query, Long currentUserId);

    // --- Update ---
    Skill updateSkill(Long userId, Long skillId, SkillRequest request);

    // --- Delete ---
    void deleteSkill(Long userId, Long skillId);
}
