package com.skillshare.skillshare.dto.exchange;

import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import java.time.Instant;

public class ExchangeRequestResponseDTO {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long skillOwnerId;
    private String skillOwnerName;
    private Long skillId;
    private String skillName;
    private String message;
    private ExchangeRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public ExchangeRequestResponseDTO(Long id, Long requesterId, String requesterName,
            Long skillOwnerId, String skillOwnerName,
            Long skillId, String skillName, String message,
            ExchangeRequestStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.skillOwnerId = skillOwnerId;
        this.skillOwnerName = skillOwnerName;
        this.skillId = skillId;
        this.skillName = skillName;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public Long getSkillOwnerId() {
        return skillOwnerId;
    }

    public String getSkillOwnerName() {
        return skillOwnerName;
    }

    public Long getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
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
