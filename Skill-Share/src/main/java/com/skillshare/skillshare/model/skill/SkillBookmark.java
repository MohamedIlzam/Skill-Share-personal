package com.skillshare.skillshare.model.skill;

import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "skill_bookmarks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_skill_bookmark_user_skill", columnNames = {"user_id", "skill_id"})
})
public class SkillBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected SkillBookmark() {
    }

    public SkillBookmark(User user, Skill skill) {
        this.user = user;
        this.skill = skill;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Skill getSkill() {
        return skill;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
