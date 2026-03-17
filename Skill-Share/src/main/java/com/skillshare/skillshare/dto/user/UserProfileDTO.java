package com.skillshare.skillshare.dto.user;

import com.skillshare.skillshare.model.user.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String email;
    private String fullName;
    private String bio;
    private String phoneNumber;
    private String location;
    private String profilePictureUrl;
    private AvailabilityStatus availabilityStatus;
    private Set<Long> mainSkillIds;
}
