package com.skillshare.skillshare.sprint3;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRatingRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sprint 3 - Exchange History Tests")
public class ExchangeHistoryServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private ExchangeRatingRepository exchangeRatingRepository;

    @InjectMocks
    private ExchangeRequestService exchangeRequestService;

    private User user;
    private User otherUser;
    private Skill skill;
    private ExchangeRequest completedRequest;

    @BeforeEach
    void setUp() {
        user = User.register("Test User", "test@user.com", "hash");
        ReflectionTestUtils.setField(user, "id", 1L);

        otherUser = User.register("Other User", "other@user.com", "hash");
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        skill = new Skill("Test Skill", SkillCategory.PROGRAMMING, SkillProficiency.BEGINNER, otherUser);
        ReflectionTestUtils.setField(skill, "id", 100L);

        completedRequest = new ExchangeRequest(user, otherUser, skill, "Message");
        ReflectionTestUtils.setField(completedRequest, "id", 500L);
        completedRequest.accept();
        completedRequest.complete();
    }

    @Test
    @DisplayName("getPersonalExchangeHistory_returnsOnlyLoggedInUsersRecords")
    void getPersonalExchangeHistory_returnsOnlyLoggedInUsersRecords() {
        when(exchangeRequestRepository.findExchangeHistoryByUserIdAndStatus(1L, ExchangeRequestStatus.COMPLETED))
                .thenReturn(List.of(completedRequest));
        when(exchangeRatingRepository.existsByExchangeIdAndRaterId(500L, 1L)).thenReturn(false);

        List<ExchangeRequestResponseDTO> history = exchangeRequestService.getCompletedExchangesHistory(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(500L, history.get(0).getId());
        assertEquals(1L, history.get(0).getRequesterId());
        
        verify(exchangeRequestRepository, times(1))
                .findExchangeHistoryByUserIdAndStatus(1L, ExchangeRequestStatus.COMPLETED);
    }

    @Test
    @DisplayName("getPersonalExchangeHistory_returnsEmptyListWhenNoHistoryExists")
    void getPersonalExchangeHistory_returnsEmptyListWhenNoHistoryExists() {
        when(exchangeRequestRepository.findExchangeHistoryByUserIdAndStatus(99L, ExchangeRequestStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        List<ExchangeRequestResponseDTO> history = exchangeRequestService.getCompletedExchangesHistory(99L);

        assertNotNull(history);
        assertTrue(history.isEmpty());
        
        verify(exchangeRequestRepository, times(1))
                .findExchangeHistoryByUserIdAndStatus(99L, ExchangeRequestStatus.COMPLETED);
    }
}
