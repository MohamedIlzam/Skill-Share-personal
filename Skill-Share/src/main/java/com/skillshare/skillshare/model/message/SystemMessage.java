package com.skillshare.skillshare.model.message;

import com.skillshare.skillshare.model.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "system_messages")
public class SystemMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected SystemMessage() {}

    public SystemMessage(User recipient, String content) {
        this.recipient = recipient;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
