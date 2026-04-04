package com.skillshare.skillshare.sprint3;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeMessage;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeMessageRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Sprint 3 Tests — ExchangeMessageService
 *
 * Covers PRD Story 5: Send and View Messages within a Skill Exchange
 *   Scenarios 1-10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Sprint 3 — ExchangeMessageService Tests")
class ExchangeMessageServiceTest {

    @Mock
    private ExchangeMessageRepository exchangeMessageRepository;
    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExchangeMessageService exchangeMessageService;

    private User requester;
    private User skillOwner;
    private User outsider;
    private Skill skill;
    private ExchangeRequest exchangeRequest;

    @BeforeEach
    void setUp() {
        requester = User.register("John Doe", "john@test.com", "hash");
        ReflectionTestUtils.setField(requester, "id", 1L);

        skillOwner = User.register("Jane Smith", "jane@test.com", "hash");
        ReflectionTestUtils.setField(skillOwner, "id", 2L);

        outsider = User.register("Outsider User", "outsider@test.com", "hash");
        ReflectionTestUtils.setField(outsider, "id", 999L);

        skill = new Skill("Java Programming", SkillCategory.PROGRAMMING, SkillProficiency.ADVANCED, skillOwner);
        ReflectionTestUtils.setField(skill, "id", 100L);

        exchangeRequest = new ExchangeRequest(requester, skillOwner, skill, "initial msg");
        ReflectionTestUtils.setField(exchangeRequest, "id", 500L);
    }

    // ─── Scenario 1: Exchange participant sends a message ────────────────

    @Test
    @DisplayName("S5-Scenario1: Requester can send a message within exchange")
    void sendMessage_byRequester_shouldSucceed() {
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Hello, can we start?");

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        ExchangeMessage savedMsg = new ExchangeMessage(exchangeRequest, requester, "Hello, can we start?");
        ReflectionTestUtils.setField(savedMsg, "id", 1000L);
        when(exchangeMessageRepository.save(any(ExchangeMessage.class))).thenReturn(savedMsg);

        MessageResponseDTO result = exchangeMessageService.sendMessage(500L, 1L, dto);

        assertNotNull(result);
        assertEquals(1000L, result.getId());
        assertEquals("Hello, can we start?", result.getContent());
        assertEquals("John Doe", result.getSenderName());
        verify(exchangeMessageRepository).save(any(ExchangeMessage.class));
    }

    @Test
    @DisplayName("S5-Scenario1: Skill owner can send a message within exchange")
    void sendMessage_byOwner_shouldSucceed() {
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Sure, let's begin!");

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(skillOwner));

        ExchangeMessage savedMsg = new ExchangeMessage(exchangeRequest, skillOwner, "Sure, let's begin!");
        ReflectionTestUtils.setField(savedMsg, "id", 1001L);
        when(exchangeMessageRepository.save(any(ExchangeMessage.class))).thenReturn(savedMsg);

        MessageResponseDTO result = exchangeMessageService.sendMessage(500L, 2L, dto);

        assertNotNull(result);
        assertEquals("Sure, let's begin!", result.getContent());
        assertEquals("Jane Smith", result.getSenderName());
    }

    // ─── Scenario 2 & 4: View messages (chronological order) ─────────────

    @Test
    @DisplayName("S5-Scenario2: Participant can view messages for their exchange")
    void getMessages_byParticipant_shouldReturnMessages() {
        ExchangeMessage msg1 = new ExchangeMessage(exchangeRequest, requester, "Hi");
        ReflectionTestUtils.setField(msg1, "id", 1000L);
        ExchangeMessage msg2 = new ExchangeMessage(exchangeRequest, skillOwner, "Hello");
        ReflectionTestUtils.setField(msg2, "id", 1001L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));
        when(exchangeMessageRepository.findByExchangeRequestIdOrderByCreatedAtAsc(500L))
                .thenReturn(List.of(msg1, msg2));

        List<MessageResponseDTO> results = exchangeMessageService.getMessages(500L, 1L);

        assertEquals(2, results.size());
        assertEquals("Hi", results.get(0).getContent());
        assertEquals("Hello", results.get(1).getContent());
    }

    // ─── Scenario 5: Only exchange participants can send messages ─────────

    @Test
    @DisplayName("S5-Scenario5: Non-participant cannot send a message")
    void sendMessage_byNonParticipant_shouldThrow() {
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Trying to sneak in");

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));

        assertThrows(AccessDeniedException.class,
                () -> exchangeMessageService.sendMessage(500L, 999L, dto));

        verify(exchangeMessageRepository, never()).save(any());
    }

    // ─── Scenario 6: Only exchange participants can view messages ─────────

    @Test
    @DisplayName("S5-Scenario6: Non-participant cannot view messages")
    void getMessages_byNonParticipant_shouldThrow() {
        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));

        assertThrows(AccessDeniedException.class,
                () -> exchangeMessageService.getMessages(500L, 999L));
    }

    // ─── Scenario 7: No messages shows empty list ────────────────────────

    @Test
    @DisplayName("S5-Scenario7: Exchange with no messages returns empty list")
    void getMessages_noMessages_shouldReturnEmptyList() {
        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));
        when(exchangeMessageRepository.findByExchangeRequestIdOrderByCreatedAtAsc(500L))
                .thenReturn(Collections.emptyList());

        List<MessageResponseDTO> results = exchangeMessageService.getMessages(500L, 1L);

        assertTrue(results.isEmpty());
    }

    // ─── Scenario 9: Messages are correctly associated ───────────────────

    @Test
    @DisplayName("S5-Scenario9: Message contains correct exchange request ID")
    void sendMessage_shouldAssociateCorrectExchangeId() {
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Test association");

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(exchangeRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        ExchangeMessage savedMsg = new ExchangeMessage(exchangeRequest, requester, "Test association");
        ReflectionTestUtils.setField(savedMsg, "id", 1002L);
        when(exchangeMessageRepository.save(any(ExchangeMessage.class))).thenReturn(savedMsg);

        MessageResponseDTO result = exchangeMessageService.sendMessage(500L, 1L, dto);

        assertEquals(500L, result.getExchangeRequestId());
        assertEquals(1L, result.getSenderId());
    }

    // ─── Scenario 10: Invalid exchange request throws exception ──────────

    @Test
    @DisplayName("S5-Scenario10: Sending to non-existent exchange throws exception")
    void sendMessage_invalidExchange_shouldThrow() {
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Ghost message");

        when(exchangeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeMessageService.sendMessage(999L, 1L, dto));
    }

    @Test
    @DisplayName("S5-Scenario10: Viewing messages for non-existent exchange throws exception")
    void getMessages_invalidExchange_shouldThrow() {
        when(exchangeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeMessageService.getMessages(999L, 1L));
    }
}
