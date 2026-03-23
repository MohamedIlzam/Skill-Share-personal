package com.skillshare.skillshare.service.skill;

import com.skillshare.skillshare.dto.skill.SkillRequest;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.SkillSpecifications;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.repository.UserProfileRepository;
import com.skillshare.skillshare.model.user.UserProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public SkillServiceImpl(SkillRepository skillRepository, UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Skill addSkill(Long userId, SkillRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // The Skill constructor handles blank/null validation internally
        Skill newSkill = new Skill(
                request.getName(),
                request.getCategory(),
                request.getProficiency(),
                owner
        );

        return skillRepository.save(newSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getSkillsByUser(Long userId) {
        // Enforce user exists first
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return skillRepository.findAllByOwnerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Skill getSkillByIdForUser(Long userId, Long skillId) {
        return skillRepository.findByIdAndOwnerId(skillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found or you do not have permission to view it."));
    }

    @Override
    public Skill updateSkill(Long userId, Long skillId, SkillRequest request) {
        // Securely fetch the skill, ensuring it belongs to the requesting user
        Skill skill = getSkillByIdForUser(userId, skillId);

        // Update details (validation happens inside the Entity method)
        skill.updateDetails(
                request.getName(),
                request.getCategory(),
                request.getProficiency()
        );

        return skillRepository.save(skill);
    }

    @Override
    public void deleteSkill(Long userId, Long skillId) {
        // Fetch to confirm ownership and existence before deletion
        Skill skill = getSkillByIdForUser(userId, skillId);
        skillRepository.delete(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getFilteredSkills(Long currentUserId, String keyword, SkillCategory category, SkillProficiency proficiency, String sortBy) {
        // Chain the specifications naturally
        Specification<Skill> spec = Specification.where(SkillSpecifications.excludeOwner(currentUserId))
                .and(SkillSpecifications.hasName(keyword))
                .and(SkillSpecifications.hasCategory(category))
                .and(SkillSpecifications.hasProficiency(proficiency))
                .and(SkillSpecifications.isOwnerAvailable());

        // Let the DB execute the initial filter and sort natively by creation time
        List<Skill> skills = skillRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Populate mainSkill status for each skill to differentiate "Offered" vs "Unavailable"
        if (!skills.isEmpty()) {
            java.util.Set<Long> userIds = skills.stream()
                    .map(s -> s.getOwner().getId())
                    .collect(java.util.stream.Collectors.toSet());
            
            List<UserProfile> profiles = userProfileRepository.findAllByUserIdIn(userIds);
            java.util.Map<Long, java.util.Set<Long>> userToMainSkillsMap = profiles.stream()
                    .collect(java.util.stream.Collectors.toMap(p -> p.getUser().getId(), UserProfile::getMainSkillIds));

            for (Skill skill : skills) {
                java.util.Set<Long> mainSkillIds = userToMainSkillsMap.get(skill.getOwner().getId());
                if (mainSkillIds != null && mainSkillIds.contains(skill.getId())) {
                    skill.setMainSkill(true);
                }
            }
        }

        // If 'Highest Proficiency' sort requested, safely post-sort Java enum ordinals
        if ("proficiency".equalsIgnoreCase(sortBy)) {
            skills.sort((a, b) -> b.getProficiency().ordinal() - a.getProficiency().ordinal());
        } else if ("lowest-proficiency".equalsIgnoreCase(sortBy)) {
            skills.sort((a, b) -> a.getProficiency().ordinal() - b.getProficiency().ordinal());
        }
        return skills;
    }
}
