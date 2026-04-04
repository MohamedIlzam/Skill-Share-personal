package com.skillshare.skillshare.dto.exchange;

import java.time.Instant;

public class RatingResponseDTO {
    private Long id;
    private Long raterId;
    private String raterName;
    private String raterProfilePictureUrl;
    private Integer ratingScore;
    private String reviewMessage;
    private Instant createdAt;

    public RatingResponseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRaterId() {
        return raterId;
    }

    public void setRaterId(Long raterId) {
        this.raterId = raterId;
    }

    public String getRaterName() {
        return raterName;
    }

    public void setRaterName(String raterName) {
        this.raterName = raterName;
    }

    public String getRaterProfilePictureUrl() {
        return raterProfilePictureUrl;
    }

    public void setRaterProfilePictureUrl(String raterProfilePictureUrl) {
        this.raterProfilePictureUrl = raterProfilePictureUrl;
    }

    public Integer getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(Integer ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
