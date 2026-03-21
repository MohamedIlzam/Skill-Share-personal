package com.skillshare.skillshare.dto.user;

import com.skillshare.skillshare.model.user.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
    private Long id;
    private String fullName;
    private String bio;
    private String location;
    private String university;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private AvailabilityStatus availabilityStatus;
    private List<String> mainSkills; // Names of the main skills
}
