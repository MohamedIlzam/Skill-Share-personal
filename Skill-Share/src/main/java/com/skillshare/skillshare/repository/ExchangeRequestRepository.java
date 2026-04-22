package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    List<ExchangeRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<ExchangeRequest> findBySkillOwnerIdOrderByCreatedAtDesc(Long skillOwnerId);

    @Query("SELECT e FROM ExchangeRequest e WHERE (e.requester.id = :userId OR e.skillOwner.id = :userId) AND e.status = :status ORDER BY e.updatedAt DESC")
    List<ExchangeRequest> findExchangeHistoryByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ExchangeRequestStatus status);

    @Query("SELECT COUNT(e) > 0 FROM ExchangeRequest e WHERE e.requester.id = :requesterId " +
            "AND e.selectedSkill.id = :skillId " +
            "AND e.status IN (:statuses)")
    boolean existsActiveRequestForSkill(
            @Param("requesterId") Long requesterId,
            @Param("skillId") Long skillId,
            @Param("statuses") List<ExchangeRequestStatus> statuses);

    // Used to check if a skill has an ongoing exchange before allowing it to be unmarked as offered
    boolean existsBySelectedSkillIdAndStatus(Long skillId, ExchangeRequestStatus status);

    // Used by ExchangeMessageService to load conversation participants
    List<ExchangeRequest> findAllByRequesterIdOrSkillOwnerId(Long requesterId, Long skillOwnerId);

    // Used by ExchangeMessageService to send direct messages
    List<ExchangeRequest> findAllByRequesterIdAndSkillOwnerId(Long requesterId, Long skillOwnerId);
}
