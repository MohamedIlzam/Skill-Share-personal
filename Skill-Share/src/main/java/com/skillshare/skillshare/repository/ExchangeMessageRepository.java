package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.exchange.ExchangeMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeMessageRepository extends JpaRepository<ExchangeMessage, Long> {
    List<ExchangeMessage> findByExchangeRequestIdOrderByCreatedAtAsc(Long exchangeRequestId);
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM ExchangeMessage m WHERE " +
            "(m.exchangeRequest.requester.id = :u1 AND m.exchangeRequest.skillOwner.id = :u2) OR " +
            "(m.exchangeRequest.requester.id = :u2 AND m.exchangeRequest.skillOwner.id = :u1) " +
            "ORDER BY m.createdAt ASC")
    List<ExchangeMessage> findMessagesBetweenUsers(@org.springframework.data.repository.query.Param("u1") Long u1, @org.springframework.data.repository.query.Param("u2") Long u2);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(m) FROM ExchangeMessage m WHERE " +
            "m.sender.id = :senderId AND " +
            "((m.exchangeRequest.requester.id = :recipientId AND m.exchangeRequest.skillOwner.id = :senderId) OR " +
            "(m.exchangeRequest.requester.id = :senderId AND m.exchangeRequest.skillOwner.id = :recipientId)) AND " +
            "m.isRead = false")
    Long countUnreadMessagesFromUser(@org.springframework.data.repository.query.Param("senderId") Long senderId, @org.springframework.data.repository.query.Param("recipientId") Long recipientId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM ExchangeMessage m WHERE " +
            "(m.exchangeRequest.requester.id = :userId OR m.exchangeRequest.skillOwner.id = :userId) AND " +
            "m.isRead = false AND m.sender.id != :userId")
    List<ExchangeMessage> findUnreadMessagesForUser(@org.springframework.data.repository.query.Param("userId") Long userId);
}
