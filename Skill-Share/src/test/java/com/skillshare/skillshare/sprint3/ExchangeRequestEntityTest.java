package com.skillshare.skillshare.sprint3;

import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 3 Tests — ExchangeRequest Entity
 * 
 * Covers PRD Stories:
 *   1. Request a Skill Exchange (Scenarios 1, 3, 4, 5)
 *   2. Manage Exchange Requests (Scenarios 3, 4, 5, 8)
 *   3. Track Exchange Request Status (Scenarios 2, 3, 4, 5)
 *   4. Mark Skill Exchange as Completed (Scenarios 1, 2, 5, 6, 7, 9)
 */
@DisplayName("Sprint 3 — ExchangeRequest Entity Tests")
class ExchangeRequestEntityTest {

    private User requester;
    private User skillOwner;
    private Skill skill;

    @BeforeEach
    void setUp() {
        requester = User.register("John Doe", "john@test.com", "hashedpw");
        skillOwner = User.register("Jane Smith", "jane@test.com", "hashedpw");
        skill = new Skill("Java Programming", SkillCategory.PROGRAMMING, SkillProficiency.ADVANCED, skillOwner);
    }

    // ─── Story 1: Request a Skill Exchange ────────────────────────────────

    @Test
    @DisplayName("S1-Scenario1: New exchange request defaults to PENDING status")
    void newRequest_shouldHavePendingStatus() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "I want to learn Java");

        assertEquals(ExchangeRequestStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("S1-Scenario3: Exchange request stores optional message")
    void newRequest_shouldStoreOptionalMessage() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Please teach me!");

        assertEquals("Please teach me!", request.getMessage());
    }

    @Test
    @DisplayName("S1-Scenario4: Exchange request accepts null message (optional)")
    void newRequest_shouldAcceptNullMessage() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, null);

        assertNull(request.getMessage());
        assertEquals(ExchangeRequestStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("S1-Scenario5: Exchange request stores correct participants and skill")
    void newRequest_shouldStoreCorrectData() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Hello");

        assertSame(requester, request.getRequester());
        assertSame(skillOwner, request.getSkillOwner());
        assertSame(skill, request.getSelectedSkill());
    }

    // ─── Story 2: Manage Exchange Requests (Accept/Reject) ───────────────

    @Test
    @DisplayName("S2-Scenario3: Skill owner can accept a PENDING request")
    void acceptPendingRequest_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");

        request.accept();

        assertEquals(ExchangeRequestStatus.ACCEPTED, request.getStatus());
    }

    @Test
    @DisplayName("S2-Scenario4: Skill owner can reject a PENDING request")
    void rejectPendingRequest_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");

        request.reject();

        assertEquals(ExchangeRequestStatus.REJECTED, request.getStatus());
    }

    @Test
    @DisplayName("S2-Scenario8: Cannot accept an already ACCEPTED request")
    void acceptAlreadyAccepted_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::accept);
        assertEquals("Only pending requests can be accepted", ex.getMessage());
    }

    @Test
    @DisplayName("S2-Scenario8: Cannot reject an already ACCEPTED request")
    void rejectAlreadyAccepted_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::reject);
        assertEquals("Only pending requests can be rejected", ex.getMessage());
    }

    @Test
    @DisplayName("S2-Scenario8: Cannot accept an already REJECTED request")
    void acceptAlreadyRejected_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.reject();

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::accept);
        assertEquals("Only pending requests can be accepted", ex.getMessage());
    }

    // ─── Story 3: Track Exchange Request Status ──────────────────────────

    @Test
    @DisplayName("S3-Scenario2: Status is correctly PENDING after creation")
    void trackStatus_pendingAfterCreation() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        assertEquals(ExchangeRequestStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("S3-Scenario3: Status is correctly ACCEPTED after accept()")
    void trackStatus_acceptedAfterAccept() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();
        assertEquals(ExchangeRequestStatus.ACCEPTED, request.getStatus());
    }

    @Test
    @DisplayName("S3-Scenario4: Status is correctly REJECTED after reject()")
    void trackStatus_rejectedAfterReject() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.reject();
        assertEquals(ExchangeRequestStatus.REJECTED, request.getStatus());
    }

    @Test
    @DisplayName("S3-Scenario5: Status is correctly COMPLETED after accept() then complete()")
    void trackStatus_completedAfterComplete() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();
        request.complete();
        assertEquals(ExchangeRequestStatus.COMPLETED, request.getStatus());
    }

    // ─── Story 4: Mark Skill Exchange as Completed ───────────────────────

    @Test
    @DisplayName("S4-Scenario1: Accepted exchange can be marked as completed")
    void completeAcceptedExchange_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();

        request.complete();

        assertEquals(ExchangeRequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    @DisplayName("S4-Scenario5/7: PENDING exchange cannot be marked as completed")
    void completePendingExchange_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::complete);
        assertEquals("Only accepted exchanges can be completed", ex.getMessage());
    }

    @Test
    @DisplayName("S4-Scenario6: REJECTED exchange cannot be marked as completed")
    void completeRejectedExchange_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.reject();

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::complete);
        assertEquals("Only accepted exchanges can be completed", ex.getMessage());
    }

    @Test
    @DisplayName("S4-Scenario9: Already COMPLETED exchange cannot be completed again")
    void completeAlreadyCompleted_shouldThrowException() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        request.accept();
        request.complete();

        IllegalStateException ex = assertThrows(IllegalStateException.class, request::complete);
        assertEquals("Only accepted exchanges can be completed", ex.getMessage());
    }
}
