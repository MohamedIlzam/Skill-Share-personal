package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.dto.user.UserProfileDTO;
import com.skillshare.skillshare.dto.user.UserProfileUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
    UserProfileDTO getProfileByUserId(Long userId);
    UserProfileDTO updateProfile(Long userId, UserProfileUpdateDTO updateDTO, MultipartFile picture);
    void toggleAvailability(Long userId);
    void createMissingProfiles();
}
