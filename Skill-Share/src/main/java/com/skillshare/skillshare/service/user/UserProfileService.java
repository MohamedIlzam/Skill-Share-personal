package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.dto.user.PublicUserDTO;
import com.skillshare.skillshare.dto.user.UserProfileDTO;
import com.skillshare.skillshare.dto.user.UserProfileUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
    UserProfileDTO getProfileByUserId(Long userId);
    UserProfileDTO updateProfile(Long userId, UserProfileUpdateDTO updateDTO, MultipartFile picture);
    void toggleAvailability(Long userId);
    void toggleMainSkill(Long userId, Long skillId);
    void createMissingProfiles();
    
    // New methods for active users and public profiles
    Page<PublicUserDTO> getActiveUsers(String search, Long currentUserId, Pageable pageable);
    PublicUserDTO getPublicProfile(Long userId);
}
