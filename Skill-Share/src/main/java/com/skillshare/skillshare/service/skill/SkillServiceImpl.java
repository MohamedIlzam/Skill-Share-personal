package com.skillshare.skillshare.service.skill;

import com.skillshare.skillshare.dto.skill.SkillRequest;
import com.skillshare.skillshare.exception.AccessDeniedException;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillServiceImpl(SkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
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
}
