package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.message.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {
    List<SystemMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
