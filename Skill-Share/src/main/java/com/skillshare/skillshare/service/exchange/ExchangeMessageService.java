package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeMessage;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.user.User;
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
                message.getCreatedAt()
        );
    }
}
