package com.skillshare.skillshare.model.exchange;

import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "exchange_messages")
public class ExchangeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_request_id", nullable = false)
    private ExchangeRequest exchangeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ExchangeMessage() {
    }

    public ExchangeMessage(ExchangeRequest exchangeRequest, User sender, String content) {
        this.exchangeRequest = exchangeRequest;
        this.sender = sender;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public ExchangeRequest getExchangeRequest() {
        return exchangeRequest;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
