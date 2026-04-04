package com.skillshare.skillshare.sprint3;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestCreateDTO;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.service.exchange.ExchangeRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Sprint 3 Tests — ExchangeRequestService
 *
 * Covers PRD Stories:
 *   1. Request a Skill Exchange (all scenarios)
 *   2. Manage Exchange Requests (accept, reject, authorization)
 *   3. Track Exchange Request Status (incoming/outgoing views)
 *   4. Mark Skill Exchange as Completed (participant enforcement)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Sprint 3 — ExchangeRequestService Tests")
class ExchangeRequestServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExchangeRequestService exchangeRequestService;

    private User requester;
    private User skillOwner;
    private Skill skill;
    private ExchangeRequestCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        requester = User.register("John Doe", "john@test.com", "hash");
        ReflectionTestUtils.setField(requester, "id", 1L);

        skillOwner = User.register("Jane Smith", "jane@test.com", "hash");
        ReflectionTestUtils.setField(skillOwner, "id", 2L);

        skill = new Skill("Java Programming", SkillCategory.PROGRAMMING, SkillProficiency.ADVANCED, skillOwner);
        ReflectionTestUtils.setField(skill, "id", 100L);

        createDTO = new ExchangeRequestCreateDTO();
        createDTO.setSkillId(100L);
        createDTO.setMessage("I'd love to learn Java!");
    }

    // ─── Story 1: Request a Skill Exchange ────────────────────────────────

    @Test
    @DisplayName("S1-Scenario1: Successfully create an exchange request")
    void createRequest_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));
        when(exchangeRequestRepository.existsActiveRequestForSkill(eq(1L), eq(100L), anyList())).thenReturn(false);

        ExchangeRequest saved = new ExchangeRequest(requester, skillOwner, skill, "I'd love to learn Java!");
        ReflectionTestUtils.setField(saved, "id", 500L);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(saved);

        ExchangeRequestResponseDTO result = exchangeRequestService.createRequest(1L, createDTO);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals(ExchangeRequestStatus.PENDING, result.getStatus());
        assertEquals("I'd love to learn Java!", result.getMessage());
        verify(exchangeRequestRepository, times(1)).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("S1-Scenario4: Create request with null message succeeds")
    void createRequest_withNullMessage_shouldSucceed() {
        createDTO.setMessage(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));
        when(exchangeRequestRepository.existsActiveRequestForSkill(eq(1L), eq(100L), anyList())).thenReturn(false);

        ExchangeRequest saved = new ExchangeRequest(requester, skillOwner, skill, null);
        ReflectionTestUtils.setField(saved, "id", 501L);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(saved);

        ExchangeRequestResponseDTO result = exchangeRequestService.createRequest(1L, createDTO);

        assertNotNull(result);
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("S1-Scenario6: Duplicate active request is prevented")
    void createRequest_duplicateActive_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));
        when(exchangeRequestRepository.existsActiveRequestForSkill(eq(1L), eq(100L), anyList())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> exchangeRequestService.createRequest(1L, createDTO));

        assertEquals("An active request already exists for this skill", ex.getMessage());
        verify(exchangeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("S1-Scenario7: User cannot request their own skill")
    void createRequest_ownSkill_shouldThrow() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(skillOwner));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> exchangeRequestService.createRequest(2L, createDTO));

        assertEquals("You cannot request your own skill", ex.getMessage());
        verify(exchangeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("S1-Scenario10: Request for non-existent skill throws ResourceNotFoundException")
    void createRequest_invalidSkill_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeRequestService.createRequest(1L, createDTO));
    }

    @Test
    @DisplayName("S1-Scenario10: Request by non-existent user throws ResourceNotFoundException")
    void createRequest_invalidUser_shouldThrow() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeRequestService.createRequest(999L, createDTO));
    }

    // ─── Story 2: Manage Exchange Requests ───────────────────────────────

    @Test
    @DisplayName("S2-Scenario3: Skill owner accepts a pending request")
    void acceptRequest_byOwner_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any())).thenReturn(request);

        ExchangeRequestResponseDTO result = exchangeRequestService.acceptRequest(500L, 2L);

        assertEquals(ExchangeRequestStatus.ACCEPTED, result.getStatus());
        verify(exchangeRequestRepository).save(request);
    }

    @Test
    @DisplayName("S2-Scenario4: Skill owner rejects a pending request")
    void rejectRequest_byOwner_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any())).thenReturn(request);

        ExchangeRequestResponseDTO result = exchangeRequestService.rejectRequest(500L, 2L);

        assertEquals(ExchangeRequestStatus.REJECTED, result.getStatus());
        verify(exchangeRequestRepository).save(request);
    }

    @Test
    @DisplayName("S2-Scenario6: Non-owner cannot accept a request")
    void acceptRequest_byNonOwner_shouldThrow() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> exchangeRequestService.acceptRequest(500L, 1L)); // requester tries

        assertEquals("You do not own the skill for this request", ex.getMessage());
    }

    @Test
    @DisplayName("S2-Scenario6: Non-owner cannot reject a request")
    void rejectRequest_byNonOwner_shouldThrow() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> exchangeRequestService.rejectRequest(500L, 1L));

        assertEquals("You do not own the skill for this request", ex.getMessage());
    }

    // ─── Story 3: Track Exchange Request Status ──────────────────────────

    @Test
    @DisplayName("S3-Scenario1: User can view incoming requests")
    void getIncomingRequests_shouldReturnOwnerRequests() {
        ExchangeRequest req = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(req, "id", 500L);

        when(exchangeRequestRepository.findBySkillOwnerIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(req));

        List<ExchangeRequestResponseDTO> results = exchangeRequestService.getIncomingRequests(2L);

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getRequesterName());
    }

    @Test
    @DisplayName("S3-Scenario2: User can view outgoing requests")
    void getOutgoingRequests_shouldReturnRequesterRequests() {
        ExchangeRequest req = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(req, "id", 500L);

        when(exchangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(req));

        List<ExchangeRequestResponseDTO> results = exchangeRequestService.getOutgoingRequests(1L);

        assertEquals(1, results.size());
        assertEquals("Jane Smith", results.get(0).getSkillOwnerName());
    }

    @Test
    @DisplayName("S3-Scenario8: User with no requests gets empty list")
    void getRequests_noRequests_shouldReturnEmptyList() {
        when(exchangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(999L))
                .thenReturn(Collections.emptyList());

        List<ExchangeRequestResponseDTO> results = exchangeRequestService.getOutgoingRequests(999L);

        assertTrue(results.isEmpty());
    }

    // ─── Story 4: Mark Skill Exchange as Completed ───────────────────────

    @Test
    @DisplayName("S4-Scenario1: Requester can complete an accepted exchange")
    void completeRequest_byRequester_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept();

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any())).thenReturn(request);

        ExchangeRequestResponseDTO result = exchangeRequestService.completeRequest(500L, 1L);

        assertEquals(ExchangeRequestStatus.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("S4-Scenario1: Skill owner can complete an accepted exchange")
    void completeRequest_byOwner_shouldSucceed() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept();

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any())).thenReturn(request);

        ExchangeRequestResponseDTO result = exchangeRequestService.completeRequest(500L, 2L);

        assertEquals(ExchangeRequestStatus.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("S4-Scenario4: Non-participant cannot complete an exchange")
    void completeRequest_byNonParticipant_shouldThrow() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept();

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> exchangeRequestService.completeRequest(500L, 999L));

        assertEquals("Only exchange participants can complete this exchange", ex.getMessage());
    }

    @Test
    @DisplayName("S4-Scenario5: PENDING exchange cannot be completed")
    void completeRequest_pendingStatus_shouldThrow() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);
        // Status is PENDING, do NOT accept

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> exchangeRequestService.completeRequest(500L, 1L));
    }

    @Test
    @DisplayName("S4-Scenario6: REJECTED exchange cannot be completed")
    void completeRequest_rejectedStatus_shouldThrow() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "msg");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.reject();

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> exchangeRequestService.completeRequest(500L, 1L));
    }

    @Test
    @DisplayName("S4-Scenario11: Non-existent request throws ResourceNotFoundException")
    void completeRequest_notFound_shouldThrow() {
        when(exchangeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeRequestService.completeRequest(999L, 1L));
    }
}
