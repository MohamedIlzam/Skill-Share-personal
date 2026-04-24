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

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(name = "is_admin_message", nullable = false)
    private boolean adminMessage = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected SystemMessage() {}

    public SystemMessage(User recipient, String content, String title, boolean adminMessage) {
        this.recipient = recipient;
        this.content = content;
        this.title = title;
        this.adminMessage = adminMessage;
        this.isRead = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(boolean adminMessage) {
        this.adminMessage = adminMessage;
    }
}
