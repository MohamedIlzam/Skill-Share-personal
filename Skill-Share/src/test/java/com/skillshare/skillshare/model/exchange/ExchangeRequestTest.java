package com.skillshare.skillshare.model.exchange;

import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRequestTest {

    private User requester;
    private User skillOwner;
    private Skill skill;
    private ExchangeRequest exchangeRequest;

    @BeforeEach
    void setUp() {
        requester = User.register("Requester", "requester@test.com", "hash");
        // Assuming we wouldn't set ID here as it's not strictly needed for Entity unit tests if we don't rely on it inside the entity.
        skillOwner = User.register("Skill Owner", "owner@test.com", "hash");
        skill = new Skill("Test Skill", com.skillshare.skillshare.model.skill.SkillCategory.PROGRAMMING, com.skillshare.skillshare.model.skill.SkillProficiency.BEGINNER, skillOwner);
        
        exchangeRequest = new ExchangeRequest(requester, skillOwner, skill, "I want to learn this");
    }

    @Test
    void testInitialStatusIsPending() {
        assertEquals(ExchangeRequestStatus.PENDING, exchangeRequest.getStatus());
    }

    @Test
    void testAcceptPendingRequest_Success() {
        exchangeRequest.accept();
        assertEquals(ExchangeRequestStatus.ACCEPTED, exchangeRequest.getStatus());
    }

    @Test
    void testRejectPendingRequest_Success() {
        exchangeRequest.reject();
        assertEquals(ExchangeRequestStatus.REJECTED, exchangeRequest.getStatus());
    }

    @Test
    void testCompleteAcceptedRequest_Success() {
        exchangeRequest.accept();
        exchangeRequest.complete();
        assertEquals(ExchangeRequestStatus.COMPLETED, exchangeRequest.getStatus());
    }

    @Test
    void testAcceptAlreadyAcceptedRequest_ThrowsException() {
        exchangeRequest.accept();
        IllegalStateException exception = assertThrows(IllegalStateException.class, exchangeRequest::accept);
        assertEquals("Only pending requests can be accepted", exception.getMessage());
    }

    @Test
    void testRejectAlreadyAcceptedRequest_ThrowsException() {
        exchangeRequest.accept();
        IllegalStateException exception = assertThrows(IllegalStateException.class, exchangeRequest::reject);
        assertEquals("Only pending requests can be rejected", exception.getMessage());
    }

    @Test
    void testCompletePendingRequest_ThrowsException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, exchangeRequest::complete);
        assertEquals("Only accepted exchanges can be completed", exception.getMessage());
    }

    @Test
    void testCompleteRejectedRequest_ThrowsException() {
        exchangeRequest.reject();
        IllegalStateException exception = assertThrows(IllegalStateException.class, exchangeRequest::complete);
        assertEquals("Only accepted exchanges can be completed", exception.getMessage());
    }
}
