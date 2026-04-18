package com.skillshare.skillshare.dto.user;

import com.skillshare.skillshare.model.user.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    private String search;
    private String category;
    private AvailabilityStatus availability;
    private Double minRating;
    private Boolean sameLocation;
    private Boolean sameUniversity;
    private String sort;
    private int page;
    private int size;
}
