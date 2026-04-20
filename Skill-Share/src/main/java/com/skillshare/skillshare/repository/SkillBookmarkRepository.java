package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.skill.SkillBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillBookmarkRepository extends JpaRepository<SkillBookmark, Long> {

    boolean existsByUserIdAndSkillId(Long userId, Long skillId);

    void deleteByUserIdAndSkillId(Long userId, Long skillId);

    List<SkillBookmark> findAllByUserId(Long userId);
}
