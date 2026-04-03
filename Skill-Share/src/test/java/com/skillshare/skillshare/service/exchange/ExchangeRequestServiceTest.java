package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestCreateDTO;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        skill = new Skill("Java Programming", com.skillshare.skillshare.model.skill.SkillCategory.PROGRAMMING, com.skillshare.skillshare.model.skill.SkillProficiency.BEGINNER, skillOwner);
        ReflectionTestUtils.setField(skill, "id", 100L);

        createDTO = new ExchangeRequestCreateDTO();
        createDTO.setSkillId(100L);
        createDTO.setMessage("Teach me Java");
    }

    @Test
    void testCreateRequest_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));
        when(exchangeRequestRepository.existsActiveRequestForSkill(eq(1L), eq(100L), anyList())).thenReturn(false);

        ExchangeRequest savedRequest = new ExchangeRequest(requester, skillOwner, skill, "Teach me Java");
        ReflectionTestUtils.setField(savedRequest, "id", 500L);
        
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(savedRequest);

        ExchangeRequestResponseDTO response = exchangeRequestService.createRequest(1L, createDTO);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Teach me Java", response.getMessage());
        assertEquals(ExchangeRequestStatus.PENDING, response.getStatus());

        verify(exchangeRequestRepository, times(1)).save(any(ExchangeRequest.class));
    }

    @Test
    void testCreateRequest_FailsWhenRequestingOwnSkill() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(skillOwner));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeRequestService.createRequest(2L, createDTO);
        });

        assertEquals("You cannot request your own skill", exception.getMessage());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    void testCreateRequest_FailsWhenDuplicateRequestExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(skill));
        when(exchangeRequestRepository.existsActiveRequestForSkill(eq(1L), eq(100L), anyList())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            exchangeRequestService.createRequest(1L, createDTO);
        });

        assertEquals("An active request already exists for this skill", exception.getMessage());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    void testAcceptRequest_Success() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Text");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(request);

        ExchangeRequestResponseDTO response = exchangeRequestService.acceptRequest(500L, 2L);

        assertEquals(ExchangeRequestStatus.ACCEPTED, response.getStatus());
        verify(exchangeRequestRepository, times(1)).save(request);
    }

    @Test
    void testAcceptRequest_FailsNotOwner() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Text");
        ReflectionTestUtils.setField(request, "id", 500L);

        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeRequestService.acceptRequest(500L, 1L); // requester tries to accept
        });

        assertEquals("You do not own the skill for this request", exception.getMessage());
    }

    @Test
    void testCompleteRequest_SuccessByRequester() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Text");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept(); // Status -> ACCEPTED
        
        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(request);

        ExchangeRequestResponseDTO response = exchangeRequestService.completeRequest(500L, 1L);

        assertEquals(ExchangeRequestStatus.COMPLETED, response.getStatus());
        verify(exchangeRequestRepository, times(1)).save(request);
    }

    @Test
    void testCompleteRequest_SuccessByOwner() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Text");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept(); // Status -> ACCEPTED
        
        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(request);

        ExchangeRequestResponseDTO response = exchangeRequestService.completeRequest(500L, 2L);

        assertEquals(ExchangeRequestStatus.COMPLETED, response.getStatus());
    }

    @Test
    void testCompleteRequest_FailsNotParticipant() {
        ExchangeRequest request = new ExchangeRequest(requester, skillOwner, skill, "Text");
        ReflectionTestUtils.setField(request, "id", 500L);
        request.accept();
        
        when(exchangeRequestRepository.findById(500L)).thenReturn(Optional.of(request));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeRequestService.completeRequest(500L, 999L); // Third-party user
        });

        assertEquals("Only exchange participants can complete this exchange", exception.getMessage());
    }
}
