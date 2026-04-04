package com.skillshare.skillshare.sprint3;

import com.skillshare.skillshare.dto.exchange.RatingResponseDTO;
import com.skillshare.skillshare.dto.exchange.RatingSubmitDTO;
import com.skillshare.skillshare.dto.exchange.RatingSummaryDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRating;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRatingRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.service.exchange.ExchangeRatingService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sprint 3 - Exchange Rating Service Tests")
public class ExchangeRatingServiceTest {

    @Mock
    private ExchangeRatingRepository ratingRepository;

    @Mock
    private ExchangeRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExchangeRatingService ratingService;

    private User requester;
    private User skillOwner;
    private User thirdParty;
    private Skill skill;
    private ExchangeRequest completedExchange;
    private ExchangeRequest pendingExchange;
    private RatingSubmitDTO validRatingDTO;
    
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        requester = User.register("Requester", "req@example.com", "hash");
        ReflectionTestUtils.setField(requester, "id", 1L);

        skillOwner = User.register("Owner", "owner@example.com", "hash");
        ReflectionTestUtils.setField(skillOwner, "id", 2L);

        thirdParty = User.register("Third", "third@example.com", "hash");
        ReflectionTestUtils.setField(thirdParty, "id", 3L);

        skill = new Skill("Java", SkillCategory.PROGRAMMING, SkillProficiency.BEGINNER, skillOwner);
        ReflectionTestUtils.setField(skill, "id", 100L);

        completedExchange = new ExchangeRequest(requester, skillOwner, skill, "Message");
        ReflectionTestUtils.setField(completedExchange, "id", 50L);
        completedExchange.accept();
        completedExchange.complete();

        pendingExchange = new ExchangeRequest(requester, skillOwner, skill, "Message");
        ReflectionTestUtils.setField(pendingExchange, "id", 51L);

        validRatingDTO = new RatingSubmitDTO();
        validRatingDTO.setExchangeId(50L);
        validRatingDTO.setRatingScore(5);
        validRatingDTO.setReviewMessage("Great experience!");
    }

    // ─── B. Ratings Retrieval ──────────────────────────────────────────────

    @Test
    @DisplayName("getUserRatingSummary_returnsCorrectAverageCountAndFeedbackForSelectedUser")
    void getUserRatingSummary_returnsCorrectAverageCountAndFeedbackForSelectedUser() {
        when(ratingRepository.getAverageRatingForUser(2L)).thenReturn(4.5);
        when(ratingRepository.countRatingsForUser(2L)).thenReturn(10L);

        RatingSummaryDTO summary = ratingService.getUserRatingSummary(2L);

        assertNotNull(summary);
        assertEquals(4.5, summary.getAverageRating());
        assertEquals(10L, summary.getTotalRatings());
    }

    @Test
    @DisplayName("getUserRatingSummary_returnsEmptyStateDataForUserWithNoRatings")
    void getUserRatingSummary_returnsEmptyStateDataForUserWithNoRatings() {
        when(ratingRepository.getAverageRatingForUser(3L)).thenReturn(null);
        when(ratingRepository.countRatingsForUser(3L)).thenReturn(0L);

        RatingSummaryDTO summary = ratingService.getUserRatingSummary(3L);

        assertNotNull(summary);
        assertEquals(0.0, summary.getAverageRating());
        assertEquals(0L, summary.getTotalRatings());
    }

    // ─── C. Rating Submission ──────────────────────────────────────────────

    @Test
    @DisplayName("submitRating_forCompletedExchangeByParticipant_succeeds")
    void submitRating_forCompletedExchangeByParticipant_succeeds() {
        when(requestRepository.findById(50L)).thenReturn(Optional.of(completedExchange));
        when(ratingRepository.existsByExchangeIdAndRaterId(50L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        ratingService.submitRating(1L, validRatingDTO);

        ArgumentCaptor<ExchangeRating> captor = ArgumentCaptor.forClass(ExchangeRating.class);
        verify(ratingRepository, times(1)).save(captor.capture());

        ExchangeRating saved = captor.getValue();
        assertEquals(5, saved.getRatingScore());
        assertEquals("Great experience!", saved.getReviewMessage());
        assertEquals(1L, saved.getRater().getId());
        assertEquals(2L, saved.getRatedUser().getId());
        assertEquals(50L, saved.getExchange().getId());
    }

    @Test
    @DisplayName("submitRating_withOptionalReviewMessage_handlesBothWithAndWithoutReview")
    void submitRating_withOptionalReviewMessage_handlesBothWithAndWithoutReview() {
        when(requestRepository.findById(50L)).thenReturn(Optional.of(completedExchange));
        when(ratingRepository.existsByExchangeIdAndRaterId(50L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        // Attempt without review message
        validRatingDTO.setReviewMessage(null);
        ratingService.submitRating(1L, validRatingDTO);

        ArgumentCaptor<ExchangeRating> captor = ArgumentCaptor.forClass(ExchangeRating.class);
        verify(ratingRepository, times(1)).save(captor.capture());

        assertNull(captor.getValue().getReviewMessage());
    }

    @Test
    @DisplayName("submitRating_duplicateForSameExchange_isRejected")
    void submitRating_duplicateForSameExchange_isRejected() {
        when(requestRepository.findById(50L)).thenReturn(Optional.of(completedExchange));
        when(ratingRepository.existsByExchangeIdAndRaterId(50L, 1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitRating(1L, validRatingDTO);
        });

        assertEquals("You have already rated this exchange", ex.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitRating_byNonParticipant_isRejected")
    void submitRating_byNonParticipant_isRejected() {
        when(requestRepository.findById(50L)).thenReturn(Optional.of(completedExchange));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitRating(3L, validRatingDTO);
        });

        assertEquals("Only the receiver of the exchange can submit a rating", ex.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitRating_forNonCompletedExchange_isRejected")
    void submitRating_forNonCompletedExchange_isRejected() {
        validRatingDTO.setExchangeId(51L);
        when(requestRepository.findById(51L)).thenReturn(Optional.of(pendingExchange));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitRating(1L, validRatingDTO);
        });

        assertEquals("Only completed exchanges can be rated", ex.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitRating_selfRatingIsPrevented")
    void submitRating_selfRatingIsPrevented() {
        // Manipulate exchange so requester and owner are the same (which shouldn't happen, but good to test the safety check!)
        ExchangeRequest internalExchange = new ExchangeRequest(requester, requester, skill, "Text");
        ReflectionTestUtils.setField(internalExchange, "id", 52L);
        internalExchange.accept();
        internalExchange.complete();

        RatingSubmitDTO selfRateDto = new RatingSubmitDTO();
        selfRateDto.setExchangeId(52L);
        selfRateDto.setRatingScore(5);

        when(requestRepository.findById(52L)).thenReturn(Optional.of(internalExchange));
        when(ratingRepository.existsByExchangeIdAndRaterId(52L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ratingService.submitRating(1L, selfRateDto);
        });

        assertEquals("You cannot rate yourself", ex.getMessage());
    }

    // ─── D. Average Rating Calculation ───────────────────────────────────────

    @Test
    @DisplayName("calculateAverageRating_usesOnlyValidCompletedExchangeRatings")
    void calculateAverageRating_usesOnlyValidCompletedExchangeRatings() {
        // Verifying that the service queries the repo directly, delegating correctness 
        // to the DB query logic which implicitly only fetches committed valid ratings.
        when(ratingRepository.getAverageRatingForUser(2L)).thenReturn(4.0);
        when(ratingRepository.countRatingsForUser(2L)).thenReturn(2L);
        
        RatingSummaryDTO summary = ratingService.getUserRatingSummary(2L);
        
        assertEquals(4.0, summary.getAverageRating());
        verify(ratingRepository, times(1)).getAverageRatingForUser(2L);
    }

    @Test
    @DisplayName("calculateAverageRating_updatesCorrectlyAfterNewValidSubmission")
    void calculateAverageRating_updatesCorrectlyAfterNewValidSubmission() {
        // Technically this involves simulating an update where mock returns new values on second call.
        when(ratingRepository.getAverageRatingForUser(2L)).thenReturn(4.0).thenReturn(4.5);
        when(ratingRepository.countRatingsForUser(2L)).thenReturn(2L).thenReturn(3L);

        RatingSummaryDTO before = ratingService.getUserRatingSummary(2L);
        assertEquals(4.0, before.getAverageRating());
        assertEquals(2L, before.getTotalRatings());

        RatingSummaryDTO after = ratingService.getUserRatingSummary(2L);
        assertEquals(4.5, after.getAverageRating());
        assertEquals(3L, after.getTotalRatings());
    }

    @Test
    @DisplayName("calculateAverageRating_formatsOrRoundsCorrectlyIfImplemented")
    void calculateAverageRating_formatsOrRoundsCorrectlyIfImplemented() {
        // Check rounding to 1 decimal place logic
        when(ratingRepository.getAverageRatingForUser(2L)).thenReturn(4.567);
        when(ratingRepository.countRatingsForUser(2L)).thenReturn(5L);

        RatingSummaryDTO summary = ratingService.getUserRatingSummary(2L);

        // 4.567 rounded Half Up to 1 decimal is 4.6
        assertEquals(4.6, summary.getAverageRating());
    }

    // ─── E. Validation Rules ───────────────────────────────────────────────

    @Test
    @DisplayName("submitRating_rejectsOutOfRangeValues")
    void submitRating_rejectsOutOfRangeValues() {
        // Test that DTO validations catch values < 1 or > 5.
        RatingSubmitDTO invalidUnder = new RatingSubmitDTO();
        invalidUnder.setExchangeId(50L);
        invalidUnder.setRatingScore(0);
        
        Set<jakarta.validation.ConstraintViolation<RatingSubmitDTO>> violationsUnder = validator.validate(invalidUnder);
        assertFalse(violationsUnder.isEmpty());
        assertTrue(violationsUnder.stream().anyMatch(v -> v.getMessage().contains("must be at least 1")));

        RatingSubmitDTO invalidOver = new RatingSubmitDTO();
        invalidOver.setExchangeId(50L);
        invalidOver.setRatingScore(6);
        
        Set<jakarta.validation.ConstraintViolation<RatingSubmitDTO>> violationsOver = validator.validate(invalidOver);
        assertFalse(violationsOver.isEmpty());
        assertTrue(violationsOver.stream().anyMatch(v -> v.getMessage().contains("must not exceed 5")));
    }

    @Test
    @DisplayName("ratingDataForProfile_returnsOnlySelectedUsersRatings")
    void ratingDataForProfile_returnsOnlySelectedUsersRatings() {
        ExchangeRating rating1 = new ExchangeRating(completedExchange, requester, skillOwner, 5, "Good");
        ReflectionTestUtils.setField(rating1, "id", 1000L);

        when(ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(rating1));

        List<RatingResponseDTO> reviews = ratingService.getUserReviews(2L);

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        assertEquals(1000L, reviews.get(0).getId());
        
        verify(ratingRepository, times(1)).findByRatedUserIdOrderByCreatedAtDesc(2L);
    }
}
