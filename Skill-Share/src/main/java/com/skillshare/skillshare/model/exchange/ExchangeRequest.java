package com.skillshare.skillshare.model.exchange;

import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "exchange_requests", uniqueConstraints = @UniqueConstraint(name = "uk_active_request", columnNames = {
        "requester_id", "skill_owner_id", "selected_skill_id", "status" }))
public class ExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_owner_id", nullable = false)
    private User skillOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_skill_id", nullable = false)
    private Skill selectedSkill;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeRequestStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ExchangeRequest() {
    }

    public ExchangeRequest(User requester, User skillOwner, Skill selectedSkill, String message) {
        this.requester = requester;
        this.skillOwner = skillOwner;
        this.selectedSkill = selectedSkill;
        this.message = message;
        this.status = ExchangeRequestStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Status updates
    public void accept() {
        if (this.status == ExchangeRequestStatus.PENDING) {
            this.status = ExchangeRequestStatus.ACCEPTED;
        } else {
            throw new IllegalStateException("Only pending requests can be accepted");
        }
    }

    public void reject() {
        if (this.status == ExchangeRequestStatus.PENDING) {
            this.status = ExchangeRequestStatus.REJECTED;
        } else {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
    }

    public void complete() {
        if (this.status == ExchangeRequestStatus.ACCEPTED) {
            this.status = ExchangeRequestStatus.COMPLETED;
        } else {
            throw new IllegalStateException("Only accepted exchanges can be completed");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getSkillOwner() {
        return skillOwner;
    }

    public Skill getSelectedSkill() {
        return selectedSkill;
    }

    public String getMessage() {
        return message;
    }

    public ExchangeRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
