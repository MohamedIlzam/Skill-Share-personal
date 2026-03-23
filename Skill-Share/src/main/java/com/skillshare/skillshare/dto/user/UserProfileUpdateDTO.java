package com.skillshare.skillshare.dto.user;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    private String fullName;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    @Pattern(regexp = "^$|[0-9\\-\\+\\s\\(\\)]{7,20}", message = "Please provide a valid phone number")
    private String phoneNumber;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 150, message = "University name must not exceed 150 characters")
    private String university;

    private MultipartFile profilePicture;

    private java.util.Set<Long> mainSkillIds;
    private String mainSkillIdsString;
}
