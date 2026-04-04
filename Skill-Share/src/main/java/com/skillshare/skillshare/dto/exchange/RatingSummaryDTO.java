package com.skillshare.skillshare.dto.exchange;

public class RatingSummaryDTO {
    private Double averageRating;
    private Long totalRatings;

    public RatingSummaryDTO() {}

    public RatingSummaryDTO(Double averageRating, Long totalRatings) {
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Long totalRatings) {
        this.totalRatings = totalRatings;
    }
}
