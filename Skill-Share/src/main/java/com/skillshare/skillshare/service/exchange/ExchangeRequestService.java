package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestCreateDTO;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.message.SystemMessage;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserStatus;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.ExchangeRatingRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.SystemMessageRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ExchangeRatingRepository exchangeRatingRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final SystemMessageRepository systemMessageRepository;

    public ExchangeRequestService(ExchangeRequestRepository exchangeRequestRepository,
            ExchangeRatingRepository exchangeRatingRepository,
            SkillRepository skillRepository,
            UserRepository userRepository,
            SystemMessageRepository systemMessageRepository) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.exchangeRatingRepository = exchangeRatingRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.systemMessageRepository = systemMessageRepository;
    }

    public ExchangeRequestResponseDTO createRequest(Long requesterId, ExchangeRequestCreateDTO dto) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (skill.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("You cannot request your own skill");
        }

        boolean exists = exchangeRequestRepository.existsActiveRequestForSkill(
                requesterId, skill.getId(), List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.ACCEPTED));

        if (exists) {
            throw new IllegalStateException("An active request already exists for this skill");
        }

        ExchangeRequest request = new ExchangeRequest(requester, skill.getOwner(), skill, dto.getMessage());
        ExchangeRequest savedRequest = exchangeRequestRepository.save(request);

        createSystemMessage(
                savedRequest.getSkillOwner().getId(),
                "New exchange request received from " + requester.getFullName() + " for skill: " + skill.getName());

        return mapToDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getIncomingRequests(Long skillOwnerId) {
        return exchangeRequestRepository.findBySkillOwnerIdOrderByCreatedAtDesc(skillOwnerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getIncomingRequests(Long skillOwnerId, ExchangeRequestStatus status) {
        List<ExchangeRequest> requests = (status == null)
                ? exchangeRequestRepository.findBySkillOwnerIdOrderByCreatedAtDesc(skillOwnerId)
                : exchangeRequestRepository.findBySkillOwnerIdAndStatusOrderByCreatedAtDesc(skillOwnerId, status);

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getOutgoingRequests(Long requesterId) {
        return exchangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getOutgoingRequests(Long requesterId, ExchangeRequestStatus status) {
        List<ExchangeRequest> requests = (status == null)
                ? exchangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                : exchangeRequestRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(requesterId, status);

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ExchangeRequestResponseDTO acceptRequest(Long requestId, Long skillOwnerId) {
        ExchangeRequest request = getRequestForOwner(requestId, skillOwnerId);
        
        if (request.getRequester().getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot accept a request from a deactivated user.");
        }
        
        request.accept();
        ExchangeRequest saved = exchangeRequestRepository.save(request);

        createSystemMessage(
                saved.getRequester().getId(),
                saved.getSkillOwner().getFullName() + " accepted your exchange request for skill: " + saved.getSelectedSkill().getName());

        return mapToDTO(saved);
    }

    public ExchangeRequestResponseDTO rejectRequest(Long requestId, Long skillOwnerId) {
        ExchangeRequest request = getRequestForOwner(requestId, skillOwnerId);
        request.reject();
        ExchangeRequest saved = exchangeRequestRepository.save(request);

        createSystemMessage(
                saved.getRequester().getId(),
                saved.getSkillOwner().getFullName() + " rejected your exchange request for skill: " + saved.getSelectedSkill().getName());

        return mapToDTO(saved);
    }

    public ExchangeRequestResponseDTO completeRequest(Long requestId, Long userId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found"));

        if (!request.getRequester().getId().equals(userId) && !request.getSkillOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only exchange participants can complete this exchange");
        }

        if (request.getRequester().getStatus() != UserStatus.ACTIVE || request.getSkillOwner().getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Exchange cannot be completed while a participant's account is deactivated.");
        }

        request.complete();
        ExchangeRequest saved = exchangeRequestRepository.save(request);

        Long otherUserId = saved.getRequester().getId().equals(userId)
                ? saved.getSkillOwner().getId()
                : saved.getRequester().getId();

        String completedByName = saved.getRequester().getId().equals(userId)
                ? saved.getRequester().getFullName()
                : saved.getSkillOwner().getFullName();

        createSystemMessage(
                otherUserId,
                completedByName + " marked the exchange as completed for skill: " + saved.getSelectedSkill().getName());

        createSystemMessage(
                saved.getRequester().getId(),
                "Reminder: Please submit a rating for your completed exchange with " + saved.getSkillOwner().getFullName() + ".");

        return mapToDTO(saved);
    }

    private void createSystemMessage(Long recipientId, String content) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        systemMessageRepository.save(new SystemMessage(recipient, content));
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getCompletedExchangesHistory(Long userId) {
        return exchangeRequestRepository.findExchangeHistoryByUserIdAndStatus(userId, ExchangeRequestStatus.COMPLETED)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getCompletedExchangesHistory(Long userId, String role) {
        if (!StringUtils.hasText(role)) {
            return getCompletedExchangesHistory(userId);
        }

        List<ExchangeRequest> exchanges;
        if ("provider".equalsIgnoreCase(role)) {
            exchanges = exchangeRequestRepository.findBySkillOwnerIdAndStatusOrderByUpdatedAtDesc(userId, ExchangeRequestStatus.COMPLETED);
        } else if ("receiver".equalsIgnoreCase(role)) {
            exchanges = exchangeRequestRepository.findByRequesterIdAndStatusOrderByUpdatedAtDesc(userId, ExchangeRequestStatus.COMPLETED);
        } else {
            exchanges = exchangeRequestRepository.findExchangeHistoryByUserIdAndStatus(userId, ExchangeRequestStatus.COMPLETED);
        }

        return exchanges.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ExchangeRequest getRequestForOwner(Long requestId, Long skillOwnerId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found"));

        if (!request.getSkillOwner().getId().equals(skillOwnerId)) {
            throw new IllegalArgumentException("You do not own the skill for this request");
        }

        return request;
    }

    private ExchangeRequestResponseDTO mapToDTO(ExchangeRequest request) {
        boolean isRated = false;
        if (request.getStatus() == ExchangeRequestStatus.COMPLETED) {
            isRated = exchangeRatingRepository.existsByExchangeIdAndRaterId(request.getId(), request.getRequester().getId());
        }

        boolean requesterActive = request.getRequester().getStatus() == UserStatus.ACTIVE;
        boolean skillOwnerActive = request.getSkillOwner().getStatus() == UserStatus.ACTIVE;

        return new ExchangeRequestResponseDTO(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getFullName(),
                request.getSkillOwner().getId(),
                request.getSkillOwner().getFullName(),
                request.getSelectedSkill().getId(),
                request.getSelectedSkill().getName(),
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                isRated,
                requesterActive,
                skillOwnerActive);
    }
}
