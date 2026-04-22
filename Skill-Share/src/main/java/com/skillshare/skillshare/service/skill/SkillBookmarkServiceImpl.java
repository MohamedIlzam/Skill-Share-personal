package com.skillshare.skillshare.service.skill;

import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillBookmark;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.SkillBookmarkRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkillBookmarkServiceImpl implements SkillBookmarkService {

    private final SkillBookmarkRepository skillBookmarkRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public SkillBookmarkServiceImpl(SkillBookmarkRepository skillBookmarkRepository,
                                    UserRepository userRepository,
                                    SkillRepository skillRepository) {
        this.skillBookmarkRepository = skillBookmarkRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getBookmarkedSkillIds(Long userId) {
        return skillBookmarkRepository.findAllByUserId(userId).stream()
                .map(b -> b.getSkill().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean toggleBookmark(Long userId, Long skillId) {
        boolean alreadyBookmarked = skillBookmarkRepository.existsByUserIdAndSkillId(userId, skillId);
        if (alreadyBookmarked) {
            skillBookmarkRepository.deleteByUserIdAndSkillId(userId, skillId);
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        skillBookmarkRepository.save(new SkillBookmark(user, skill));
        return true;
    }
}
