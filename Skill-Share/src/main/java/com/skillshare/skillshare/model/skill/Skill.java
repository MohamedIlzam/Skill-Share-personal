package com.skillshare.skillshare.model.skill;

import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkillProficiency proficiency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Transient
    private boolean mainSkill;

    // Default constructor for JPA
    protected Skill() {}

    public Skill(String name, SkillCategory category, SkillProficiency proficiency, User owner) {
        this.name = requireNonBlank(name, "Skill name");
        this.category = requireNonNull(category, "Category");
        this.proficiency = requireNonNull(proficiency, "Proficiency");
        this.owner = requireNonNull(owner, "Owner");
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

    // --- Business Logic Methods ---

    public void updateDetails(String newName, SkillCategory newCategory, SkillProficiency newProficiency) {
        this.name = requireNonBlank(newName, "Skill name");
        this.category = requireNonNull(newCategory, "Category");
        this.proficiency = requireNonNull(newProficiency, "Proficiency");
    }

    // --- Validation Helpers ---

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T obj, String field) {
        if (obj == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        return obj;
    }

    // --- Getters ---

    public Long getId() { return id; }
    public String getName() { return name; }
    public SkillCategory getCategory() { return category; }
    public SkillProficiency getProficiency() { return proficiency; }
    public User getOwner() { return owner; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isMainSkill() { return mainSkill; }
    public void setMainSkill(boolean mainSkill) { this.mainSkill = mainSkill; }
}
