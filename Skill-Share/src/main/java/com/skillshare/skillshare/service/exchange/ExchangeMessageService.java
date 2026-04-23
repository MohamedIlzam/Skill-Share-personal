package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeMessage;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserStatus;
import com.skillshare.skillshare.repository.ExchangeMessageRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExchangeMessageService {

    private final ExchangeMessageRepository exchangeMessageRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;

    public ExchangeMessageService(ExchangeMessageRepository exchangeMessageRepository,
                                  ExchangeRequestRepository exchangeRequestRepository,
                                  UserRepository userRepository) {
        this.exchangeMessageRepository = exchangeMessageRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.userRepository = userRepository;
    }

    public MessageResponseDTO sendMessage(Long requestId, Long senderId, MessageCreateDTO dto) {
        ExchangeRequest request = getValidatedRequest(requestId, senderId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExchangeMessage message = new ExchangeMessage(request, sender, dto.getContent());
        ExchangeMessage savedMessage = exchangeMessageRepository.save(message);

        return mapToDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getMessages(Long requestId, Long userId) {
        getValidatedRequest(requestId, userId); // Validates user is participant
        return exchangeMessageRepository.findByExchangeRequestIdOrderByCreatedAtAsc(requestId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ExchangeRequest getValidatedRequest(Long requestId, Long userId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found"));

        boolean isRequester = request.getRequester().getId().equals(userId);
        boolean isOwner = request.getSkillOwner().getId().equals(userId);

        if (!isRequester && !isOwner) {
            throw new AccessDeniedException("Only exchange participants can access these messages");
        }
        return request;
    }

    @Transactional(readOnly = true)
    public List<com.skillshare.skillshare.dto.message.ConversationDTO> getUserConversations(Long userId) {
        // Find all unique users the current user has had an exchange with
        List<ExchangeRequest> requests = exchangeRequestRepository.findAllByRequesterIdOrSkillOwnerId(userId, userId);
        
        java.util.Map<Long, User> participantsMap = new java.util.HashMap<>();
        for (ExchangeRequest req : requests) {
            User other = req.getRequester().getId().equals(userId) ? req.getSkillOwner() : req.getRequester();
            if (other.getStatus() == UserStatus.ACTIVE) {
                participantsMap.put(other.getId(), other);
            }
        }

        return participantsMap.values().stream().map(other -> {
            List<ExchangeMessage> messages = exchangeMessageRepository.findMessagesBetweenUsers(userId, other.getId());
            ExchangeMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            Long unreadCount = exchangeMessageRepository.countUnreadMessagesFromUser(other.getId(), userId);

            String profilePic = other.getProfile() != null ? other.getProfile().getProfilePictureUrl() : null;

            return com.skillshare.skillshare.dto.message.ConversationDTO.builder()
                    .otherUserId(other.getId())
                    .otherUserName(other.getFullName())
                    .otherUserProfilePic(profilePic)
                    .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "No messages yet")
                    .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                    .unreadCount(unreadCount)
                    .build();
        }).sorted((c1, c2) -> {
            if (c1.getLastMessageTime() == null && c2.getLastMessageTime() == null) return 0;
            if (c1.getLastMessageTime() == null) return 1;
            if (c2.getLastMessageTime() == null) return -1;
            return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getChatHistory(Long userId, Long otherUserId) {
        return exchangeMessageRepository.findMessagesBetweenUsers(userId, otherUserId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long userId, Long otherUserId) {
        List<ExchangeMessage> unread = exchangeMessageRepository.findUnreadMessagesForUser(userId)
                .stream()
                .filter(m -> m.getSender().getId().equals(otherUserId))
                .collect(Collectors.toList());
        
        unread.forEach(m -> m.setRead(true));
        exchangeMessageRepository.saveAll(unread);
    }

    public MessageResponseDTO sendDirectMessage(Long senderId, Long recipientId, MessageCreateDTO dto) {
        // Find most recent exchange request between these two users to anchor the message
        List<ExchangeRequest> requests = exchangeRequestRepository.findAllByRequesterIdAndSkillOwnerId(senderId, recipientId);
        requests.addAll(exchangeRequestRepository.findAllByRequesterIdAndSkillOwnerId(recipientId, senderId));
        
        if (requests.isEmpty()) {
            throw new IllegalStateException("No active exchange request found between users to send a message.");
        }

        ExchangeRequest latestRequest = requests.stream()
                .sorted(java.util.Comparator.comparing(ExchangeRequest::getUpdatedAt).reversed())
                .findFirst()
                .get();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExchangeMessage message = new ExchangeMessage(latestRequest, sender, dto.getContent());
        ExchangeMessage savedMessage = exchangeMessageRepository.save(message);

        return mapToDTO(savedMessage);
    }

    private MessageResponseDTO mapToDTO(ExchangeMessage message) {
        String profilePic = message.getSender().getProfile() != null 
                ? message.getSender().getProfile().getProfilePictureUrl() 
                : null;
        return new MessageResponseDTO(
                message.getId(),
                message.getExchangeRequest().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                profilePic,
                message.getContent(),
                message.getCreatedAt(),
                message.isRead()
        );
    }
}
