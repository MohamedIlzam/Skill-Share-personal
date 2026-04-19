package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.dto.user.PublicUserDTO;
import com.skillshare.skillshare.dto.user.UserProfileDTO;
import com.skillshare.skillshare.dto.user.UserProfileUpdateDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.user.AvailabilityStatus;
import com.skillshare.skillshare.model.user.UserProfile;
import com.skillshare.skillshare.repository.UserProfileRepository;
import com.skillshare.skillshare.repository.ExchangeRatingRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final com.skillshare.skillshare.repository.SkillRepository skillRepository;
    private final ExchangeRatingRepository exchangeRatingRepository;
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
        profile.setUniversity(updateDTO.getUniversity());

        if (picture != null && !picture.isEmpty()) {
            validateImage(picture);
            String fileName = storeFile(picture);
            profile.setProfilePictureUrl("/images/profiles/" + fileName);
        }

        // --- Main Skills Selection Logic ---
        java.util.Set<Long> selectedSkillIds = new java.util.HashSet<>();
        if (updateDTO.getMainSkillIdsString() != null && !updateDTO.getMainSkillIdsString().isBlank()) {
            java.util.Arrays.stream(updateDTO.getMainSkillIdsString().split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .forEach(selectedSkillIds::add);
        } else if (updateDTO.getMainSkillIds() != null) {
            selectedSkillIds.addAll(updateDTO.getMainSkillIds());
        }

        if (!selectedSkillIds.isEmpty()) {
            // Fetch all valid skill IDs belonging to this user
            java.util.Set<Long> ownedSkillIds = skillRepository.findAllByOwnerId(userId)
                    .stream()
                    .map(com.skillshare.skillshare.model.skill.Skill::getId)
                    .collect(java.util.stream.Collectors.toSet());

            // Ensure every selected ID is actually owned by the user
            boolean allOwned = ownedSkillIds.containsAll(selectedSkillIds);
            if (!allOwned) {
                throw new IllegalArgumentException("One or more selected skills do not belong to you.");
            }

            // Limit check: Max 5 skills
            if (selectedSkillIds.size() > 5) {
                throw new IllegalArgumentException("You can only select up to 5 main skills.");
            }

            // Update profile
            profile.getMainSkillIds().clear();
            profile.getMainSkillIds().addAll(selectedSkillIds);
        } else {
            profile.getMainSkillIds().clear();
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

    @Override
    @Transactional
    public void toggleMainSkill(Long userId, Long skillId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        if (profile.getMainSkillIds().contains(skillId)) {
            profile.getMainSkillIds().remove(skillId);
        } else {
            // Validate ownership before adding
            skillRepository.findByIdAndOwnerId(skillId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("You do not own this skill."));

            if (profile.getMainSkillIds().size() >= 5) {
                throw new IllegalStateException("You can only highlight up to 5 main skills.");
            }
            profile.getMainSkillIds().add(skillId);
        }
        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicUserDTO> getActiveUsers(com.skillshare.skillshare.dto.user.UserFilterDTO filterDTO, Long currentUserId) {
        java.util.List<UserProfile> profiles = userProfileRepository.searchByKeywordExceptUser(filterDTO.getSearch(), currentUserId);

        // Pre-filter by Skill Category before mapping
        if (filterDTO.getCategory() != null && !filterDTO.getCategory().isBlank()) {
            profiles = profiles.stream().filter(p -> {
                if (p.getUser() == null) return false;
                return skillRepository.findAllByOwnerId(p.getUser().getId()).stream()
                        .anyMatch(s -> s.getCategory().name().equalsIgnoreCase(filterDTO.getCategory()) || 
                                       s.getName().equalsIgnoreCase(filterDTO.getCategory()));
            }).collect(java.util.stream.Collectors.toList());
        }

        // Map to DTOs
        java.util.List<PublicUserDTO> dtoList = profiles.stream()
                .map(this::mapToPublicDTO)
                .collect(java.util.stream.Collectors.toList());

        // Setup current user profile for relative filtering
        UserProfileDTO currentUserProfile = null;
        if (currentUserId != null && currentUserId != -1L) {
            try {
                currentUserProfile = getProfileByUserId(currentUserId);
            } catch (ResourceNotFoundException e) {
                // Ignore
            }
        }
        final UserProfileDTO finalCurrentUserProfile = currentUserProfile;

        // Apply remaining filters via Stream API
        java.util.stream.Stream<PublicUserDTO> stream = dtoList.stream();

        if (filterDTO.getAvailability() != null) {
            stream = stream.filter(u -> u.getAvailabilityStatus() == filterDTO.getAvailability());
        }

        if (filterDTO.getMinRating() != null) {
            stream = stream.filter(u -> u.getAverageRating() != null && u.getAverageRating() >= filterDTO.getMinRating());
        }

        if (Boolean.TRUE.equals(filterDTO.getSameLocation()) && finalCurrentUserProfile != null && finalCurrentUserProfile.getLocation() != null && !finalCurrentUserProfile.getLocation().isBlank()) {
            stream = stream.filter(u -> finalCurrentUserProfile.getLocation().equalsIgnoreCase(u.getLocation()));
        }

        if (Boolean.TRUE.equals(filterDTO.getSameUniversity()) && finalCurrentUserProfile != null && finalCurrentUserProfile.getUniversity() != null && !finalCurrentUserProfile.getUniversity().isBlank()) {
            stream = stream.filter(u -> finalCurrentUserProfile.getUniversity().equalsIgnoreCase(u.getUniversity()));
        }

        // Apply custom sorting
        if ("highest_rating".equals(filterDTO.getSort())) {
            stream = stream.sorted((a, b) -> {
                double r1 = a.getAverageRating() != null ? a.getAverageRating() : -1.0;
                double r2 = b.getAverageRating() != null ? b.getAverageRating() : -1.0;
                return Double.compare(r2, r1);
            });
        } else if ("lowest_rating".equals(filterDTO.getSort())) {
            stream = stream.sorted((a, b) -> {
                if (a.getAverageRating() == null && b.getAverageRating() != null) return 1;
                if (b.getAverageRating() == null && a.getAverageRating() != null) return -1;
                if (a.getAverageRating() == null && b.getAverageRating() == null) return 0;
                return Double.compare(a.getAverageRating(), b.getAverageRating());
            });
        } else if ("name_asc".equals(filterDTO.getSort())) {
            stream = stream.sorted(java.util.Comparator.comparing(PublicUserDTO::getFullName, String.CASE_INSENSITIVE_ORDER));
        }

        java.util.List<PublicUserDTO> filteredList = stream.collect(java.util.stream.Collectors.toList());

        // Perform manual pagination
        int page = filterDTO.getPage();
        int size = filterDTO.getSize();
        if (size <= 0) size = 12; // default
        if (page < 0) page = 0;
        
        int start = page * size;
        int end = Math.min((start + size), filteredList.size());

        java.util.List<PublicUserDTO> pageContent;
        if (start <= end) {
            pageContent = filteredList.subList(start, end);
        } else {
            pageContent = new java.util.ArrayList<>();
        }

        return new org.springframework.data.domain.PageImpl<>(pageContent, org.springframework.data.domain.PageRequest.of(page, size), filteredList.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicUserDTO getPublicProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
        return mapToPublicDTO(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<PublicUserDTO> getTopRatedUsers(int limit) {
        // Fetch IDs ordered by rating performance
        java.util.List<Long> ratedUserIds = exchangeRatingRepository.findTopRatedUserIds(org.springframework.data.domain.PageRequest.of(0, limit));
        
        return ratedUserIds.stream()
                .map(userId -> {
                    try {
                        // Map each ID to a full public profile
                        return getPublicProfile(userId);
                    } catch (ResourceNotFoundException e) {
                        // Log skipped user if profile missing for some reason
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    private PublicUserDTO mapToPublicDTO(UserProfile profile) {
        String bio = profile.getBio();
        if (bio != null && bio.isBlank()) bio = null;
        String fullName = profile.getFullName();
        String location = profile.getLocation();
        String university = profile.getUniversity();
        String email = (profile.getUser() != null) ? profile.getUser().getEmail() : null;
        String phoneNumber = profile.getPhoneNumber();
        String profilePictureUrl = profile.getProfilePictureUrl();
        AvailabilityStatus availabilityStatus = profile.getAvailabilityStatus();

        java.util.List<String> mainSkillNames = new java.util.ArrayList<>();
        java.util.List<String> otherSkillNames = new java.util.ArrayList<>();

        if (profile.getUser() != null) {
            java.util.List<com.skillshare.skillshare.model.skill.Skill> allSkills = 
                    skillRepository.findAllByOwnerId(profile.getUser().getId());
            
            java.util.Set<Long> mainSkillIds = profile.getMainSkillIds();

            for (com.skillshare.skillshare.model.skill.Skill skill : allSkills) {
                if (mainSkillIds != null && mainSkillIds.contains(skill.getId())) {
                    mainSkillNames.add(skill.getName());
                } else {
                    otherSkillNames.add(skill.getName());
                }
            }
        }

        Double averageRating = null;
        Long totalRatings = 0L;
        if (profile.getUser() != null) {
            Double avg = exchangeRatingRepository.getAverageRatingForUser(profile.getUser().getId());
            totalRatings = exchangeRatingRepository.countRatingsForUser(profile.getUser().getId());
            if (avg != null && avg > 0) {
                averageRating = new BigDecimal(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }
        }

        return PublicUserDTO.builder()
                .id(profile.getUser() != null ? profile.getUser().getId() : null)
                .fullName(fullName)
                .bio(bio)
                .location(location)
                .university(university)
                .email(email)
                .phoneNumber(phoneNumber)
                .profilePictureUrl(profilePictureUrl)
                .availabilityStatus(availabilityStatus)
                .mainSkills(mainSkillNames)
                .otherSkills(otherSkillNames)
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .build();
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
                .university(profile.getUniversity())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .availabilityStatus(profile.getAvailabilityStatus())
                .mainSkillIds(profile.getMainSkillIds())
                .build();
    }
}
