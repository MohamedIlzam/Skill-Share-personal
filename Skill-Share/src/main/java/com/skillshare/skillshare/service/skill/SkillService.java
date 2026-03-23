package com.skillshare.skillshare.service.skill;

import com.skillshare.skillshare.dto.skill.SkillRequest;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;

import java.util.List;

public interface SkillService {

    // --- Create ---
    Skill addSkill(Long userId, SkillRequest request);

    // --- Read ---
    List<Skill> getSkillsByUser(Long userId);
    
    Skill getSkillByIdForUser(Long userId, Long skillId);

    // --- Browse / Search (Feature 3 & 5) ---
    List<Skill> getFilteredSkills(Long currentUserId, String keyword, SkillCategory category, SkillProficiency proficiency, String sortBy);

    // --- Update ---
    Skill updateSkill(Long userId, Long skillId, SkillRequest request);

    // --- Delete ---
    void deleteSkill(Long userId, Long skillId);
}
