package com.skillshare.skillshare.dto.skill;

import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SkillRequest {

    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Please select a category")
    private SkillCategory category;

    @NotNull(message = "Please select a proficiency level")
    private SkillProficiency proficiency;

    // Default constructor for Jackson/Spring Binding
    public SkillRequest() {}

    public SkillRequest(String name, SkillCategory category, SkillProficiency proficiency) {
        this.name = name;
        this.category = category;
        this.proficiency = proficiency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }
}
