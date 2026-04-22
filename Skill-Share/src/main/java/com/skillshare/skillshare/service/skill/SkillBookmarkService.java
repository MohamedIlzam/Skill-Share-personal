package com.skillshare.skillshare.service.skill;

import java.util.Set;

public interface SkillBookmarkService {

    Set<Long> getBookmarkedSkillIds(Long userId);

    boolean toggleBookmark(Long userId, Long skillId);
}
