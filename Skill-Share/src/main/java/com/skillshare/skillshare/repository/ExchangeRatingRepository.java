package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.exchange.ExchangeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangeRatingRepository extends JpaRepository<ExchangeRating, Long> {

    boolean existsByExchangeIdAndRaterId(Long exchangeId, Long raterId);

    @Query("SELECT AVG(r.ratingScore) FROM ExchangeRating r WHERE r.ratedUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM ExchangeRating r WHERE r.ratedUser.id = :userId")
    Long countRatingsForUser(@Param("userId") Long userId);

    List<ExchangeRating> findByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId);
    
    org.springframework.data.domain.Page<ExchangeRating> findByRatedUserId(Long ratedUserId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<ExchangeRating> findByRatedUserIdAndRatingScoreGreaterThanEqual(
            Long ratedUserId,
            Integer ratingScore,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Retrieves IDs of users with the highest average ratings.
     * Groups by user ID and orders by average score (descending) and total count (descending).
     * 
     * @param pageable specifies the limit of users to return
     * @return a list of top rated user IDs
     */
    @Query("SELECT r.ratedUser.id FROM ExchangeRating r GROUP BY r.ratedUser.id HAVING AVG(r.ratingScore) >= 4.0 ORDER BY AVG(r.ratingScore) DESC, COUNT(r) DESC")
    List<Long> findTopRatedUserIds(org.springframework.data.domain.Pageable pageable);
}
