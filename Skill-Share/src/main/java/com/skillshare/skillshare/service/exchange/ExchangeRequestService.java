package com.skillshare.skillshare.service.exchange;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestCreateDTO;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public ExchangeRequestService(ExchangeRequestRepository exchangeRequestRepository,
            SkillRepository skillRepository,
            UserRepository userRepository) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
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

        return mapToDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getIncomingRequests(Long skillOwnerId) {
        return exchangeRequestRepository.findBySkillOwnerIdOrderByCreatedAtDesc(skillOwnerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getOutgoingRequests(Long requesterId) {
        return exchangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExchangeRequestResponseDTO acceptRequest(Long requestId, Long skillOwnerId) {
        ExchangeRequest request = getRequestForOwner(requestId, skillOwnerId);
        request.accept();
        return mapToDTO(exchangeRequestRepository.save(request));
    }

    public ExchangeRequestResponseDTO rejectRequest(Long requestId, Long skillOwnerId) {
        ExchangeRequest request = getRequestForOwner(requestId, skillOwnerId);
        request.reject();
        return mapToDTO(exchangeRequestRepository.save(request));
    }

    public ExchangeRequestResponseDTO completeRequest(Long requestId, Long userId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found"));

        if (!request.getRequester().getId().equals(userId) && !request.getSkillOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only exchange participants can complete this exchange");
        }

        request.complete();
        return mapToDTO(exchangeRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getCompletedExchangesHistory(Long userId) {
        return exchangeRequestRepository.findExchangeHistoryByUserIdAndStatus(userId, ExchangeRequestStatus.COMPLETED)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                request.getUpdatedAt());
    }
}
