package com.skillshare.skillshare.model.exchange;

import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "exchange_ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_exchange_rater", columnNames = {"exchange_id", "rater_id"})
})
public class ExchangeRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false)
    private ExchangeRequest exchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_user_id", nullable = false)
    private User ratedUser;

    @Column(nullable = false)
    private Integer ratingScore;

    @Column(length = 2000)
    private String reviewMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ExchangeRating() {
    }

    public ExchangeRating(ExchangeRequest exchange, User rater, User ratedUser, Integer ratingScore, String reviewMessage) {
        if (ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        this.exchange = exchange;
        this.rater = rater;
        this.ratedUser = ratedUser;
        this.ratingScore = ratingScore;
        this.reviewMessage = reviewMessage;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public ExchangeRequest getExchange() {
        return exchange;
    }

    public User getRater() {
        return rater;
    }

    public User getRatedUser() {
        return ratedUser;
    }

    public Integer getRatingScore() {
        return ratingScore;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
