package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.exchange.ExchangeMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeMessageRepository extends JpaRepository<ExchangeMessage, Long> {
    List<ExchangeMessage> findByExchangeRequestIdOrderByCreatedAtAsc(Long exchangeRequestId);
}
