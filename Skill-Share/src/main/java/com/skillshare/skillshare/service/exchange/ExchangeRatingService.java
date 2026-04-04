package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.RatingResponseDTO;
import com.skillshare.skillshare.dto.exchange.RatingSubmitDTO;
import com.skillshare.skillshare.dto.exchange.RatingSummaryDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRating;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRatingRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeRatingService {

    private final ExchangeRatingRepository ratingRepository;
    private final ExchangeRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ExchangeRatingService(ExchangeRatingRepository ratingRepository,
                                 ExchangeRequestRepository requestRepository,
                                 UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void submitRating(Long raterId, RatingSubmitDTO ratingDto) {
        ExchangeRequest exchange = requestRepository.findById(ratingDto.getExchangeId())
                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));

        if (exchange.getStatus() != ExchangeRequestStatus.COMPLETED) {
            throw new IllegalStateException("Only completed exchanges can be rated");
        }

        if (!exchange.getRequester().getId().equals(raterId)) {
            throw new IllegalStateException("Only the receiver of the exchange can submit a rating");
        }

        if (ratingRepository.existsByExchangeIdAndRaterId(exchange.getId(), raterId)) {
            throw new IllegalStateException("You have already rated this exchange");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new IllegalArgumentException("Rater not found"));
        User ratedUser = exchange.getSkillOwner(); // rater is the requester, so the owner is the rated user

        if (rater.getId().equals(ratedUser.getId())) {
            throw new IllegalStateException("You cannot rate yourself");
        }

        ExchangeRating rating = new ExchangeRating(
                exchange,
                rater,
                ratedUser,
                ratingDto.getRatingScore(),
                ratingDto.getReviewMessage()
        );

        ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public RatingSummaryDTO getUserRatingSummary(Long userId) {
        Double avg = ratingRepository.getAverageRatingForUser(userId);
        Long count = ratingRepository.countRatingsForUser(userId);

        if (avg == null || count == null || count == 0) {
            return new RatingSummaryDTO(0.0, 0L);
        }

        // Round to 1 decimal place
        BigDecimal bd = new BigDecimal(avg).setScale(1, RoundingMode.HALF_UP);
        return new RatingSummaryDTO(bd.doubleValue(), count);
    }

    @Transactional(readOnly = true)
    public List<RatingResponseDTO> getUserReviews(Long userId) {
        List<ExchangeRating> ratings = ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId);
        return ratings.stream().limit(5).map(r -> {
            RatingResponseDTO dto = new RatingResponseDTO();
            dto.setId(r.getId());
            dto.setRaterId(r.getRater().getId());
            dto.setRaterName(r.getRater().getFullName());
            if (r.getRater().getProfile() != null) {
                dto.setRaterProfilePictureUrl(r.getRater().getProfile().getProfilePictureUrl());
            }
            dto.setRatingScore(r.getRatingScore());
            dto.setReviewMessage(r.getReviewMessage());
            dto.setCreatedAt(r.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RatingResponseDTO> getPaginatedUserReviews(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ExchangeRating> ratingsPage = ratingRepository.findByRatedUserId(userId, pageable);
        
        return ratingsPage.map(r -> {
            RatingResponseDTO dto = new RatingResponseDTO();
            dto.setId(r.getId());
            dto.setRaterId(r.getRater().getId());
            dto.setRaterName(r.getRater().getFullName());
            if (r.getRater().getProfile() != null) {
                dto.setRaterProfilePictureUrl(r.getRater().getProfile().getProfilePictureUrl());
            }
            dto.setRatingScore(r.getRatingScore());
            dto.setReviewMessage(r.getReviewMessage());
            dto.setCreatedAt(r.getCreatedAt());
            return dto;
        });
    }
}
