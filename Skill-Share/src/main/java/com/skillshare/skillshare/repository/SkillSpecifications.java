package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SkillSpecifications {

    public static Specification<Skill> hasName(String nameKeyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(nameKeyword)) return null;
            return cb.like(cb.lower(root.get("name")), "%" + nameKeyword.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Skill> hasCategory(SkillCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Skill> hasProficiency(SkillProficiency proficiency) {
        return (root, query, cb) -> proficiency == null ? null : cb.equal(root.get("proficiency"), proficiency);
    }

    public static Specification<Skill> excludeOwner(Long ownerId) {
        return (root, query, cb) -> cb.notEqual(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Skill> isOwnerAvailable() {
        return (root, query, cb) -> cb.equal(root.get("owner").get("profile").get("availabilityStatus"), 
                com.skillshare.skillshare.model.user.AvailabilityStatus.AVAILABLE);
    }
}
