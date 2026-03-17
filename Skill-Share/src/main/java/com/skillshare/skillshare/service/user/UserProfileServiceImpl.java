package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.dto.user.UserProfileDTO;
import com.skillshare.skillshare.dto.user.UserProfileUpdateDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.user.AvailabilityStatus;
import com.skillshare.skillshare.model.user.UserProfile;
import com.skillshare.skillshare.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final com.skillshare.skillshare.repository.UserRepository userRepository;
    private static final String UPLOAD_DIRECTORY = "src/main/resources/static/images/profiles/";

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
        return mapToDTO(profile);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateDTO updateDTO, MultipartFile picture) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        profile.setFullName(updateDTO.getFullName());
        profile.setBio(updateDTO.getBio());
        profile.setPhoneNumber(updateDTO.getPhoneNumber());
        profile.setLocation(updateDTO.getLocation());

        if (picture != null && !picture.isEmpty()) {
            validateImage(picture);
            String fileName = storeFile(picture);
            profile.setProfilePictureUrl("/images/profiles/" + fileName);
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        return mapToDTO(savedProfile);
    }

    @Override
    @Transactional
    public void toggleAvailability(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        profile.setAvailabilityStatus(
                profile.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE 
                ? AvailabilityStatus.UNAVAILABLE 
                : AvailabilityStatus.AVAILABLE
        );
        userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void createMissingProfiles() {
        java.util.List<com.skillshare.skillshare.model.user.User> usersWithoutProfile = userRepository.findAllUsersWithoutProfile();
        for (com.skillshare.skillshare.model.user.User user : usersWithoutProfile) {
            UserProfile profile = new UserProfile(user, user.getFullName());
            user.setProfile(profile);
            userProfileRepository.save(profile);
        }
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPG and PNG images are allowed");
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store profile picture", e);
        }
    }

    private UserProfileDTO mapToDTO(UserProfile profile) {
        return UserProfileDTO.builder()
                .id(profile.getId())
                .email(profile.getUser().getEmail())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .location(profile.getLocation())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .availabilityStatus(profile.getAvailabilityStatus())
                .mainSkillIds(profile.getMainSkillIds())
                .build();
    }
}
