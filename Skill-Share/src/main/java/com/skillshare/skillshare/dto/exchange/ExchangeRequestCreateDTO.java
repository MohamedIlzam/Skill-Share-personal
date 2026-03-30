package com.skillshare.skillshare.dto.exchange;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ExchangeRequestCreateDTO {

    @NotNull(message = "Skill ID cannot be null")
    private Long skillId;

    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;

    public ExchangeRequestCreateDTO() {
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
