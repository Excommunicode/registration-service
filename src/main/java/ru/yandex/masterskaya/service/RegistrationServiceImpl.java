package ru.yandex.masterskaya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterskaya.client.EventClient;
import ru.yandex.masterskaya.client.UserClient;
import ru.yandex.masterskaya.dto.EventDto;
import ru.yandex.masterskaya.dto.EventTeamDto;
import ru.yandex.masterskaya.dto.ManagerDto;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.dto.UserResponseDTO;
import ru.yandex.masterskaya.enums.Status;
import ru.yandex.masterskaya.exception.BadRequestException;
import ru.yandex.masterskaya.exception.NotFoundException;
import ru.yandex.masterskaya.mapper.RegistrationMapper;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.model.StatusProjection;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.contract.RegistrationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class RegistrationServiceImpl implements RegistrationService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final RegistrationMapper registrationMapper;
    private final EventPublisherService eventPublisherService;
    private final RegistrationRepository registrationRepository;

    @Override
    @Transactional
    public RegistrationResponseDTO addRegistration(RegistrationCreateRequestDto registrationCreateRequestDto) {
        log.info("Starting method addRegistration. Received registrationCreateRequestDto: {}", registrationCreateRequestDto);

        eventClient.getEventById(registrationCreateRequestDto.eventId());
        userClient.findByEmail(registrationCreateRequestDto.email());

        String password = UUID.randomUUID().toString().substring(0, 4);
        Registration registration = registrationMapper.toModel(registrationCreateRequestDto, password);


        Registration savedRegistration = registrationRepository.saveAndReturn(registration);
        log.info("Registration successfully saved with Number: {} and details: {}", savedRegistration.getNumber(), savedRegistration);

        return registrationMapper.toDto(savedRegistration);
    }

    @Override
    @Transactional
    public RegistrationUpdateRequestDto updateRegistration(RegistrationUpdateRequestDto registrationUpdateRequestDto) {
        log.info("Starting method updateRegistration for eventId:  with data: {}", registrationUpdateRequestDto);

        Registration registration = registrationMapper.toModelAfterDto(registrationUpdateRequestDto);

        Registration updatedRegistration = registrationRepository.updateByEventIdAndNumberAndPassword(registration);

        log.info("Registration successfully updated. Updated details: {}", updatedRegistration);
        return registrationMapper.toFullDto(updatedRegistration);
    }

    @Override
    public RegistrationCreateRequestDto getRegistration(Long id) {
        log.info("Starting method getRegistration with ID: {}", id);

        RegistrationCreateRequestDto registration = registrationRepository.findByIdDto(id)
                .orElseThrow(() -> {
                    log.warn("RegistrationDto not found for ID: {}", id);
                    return NotFoundException.builder()
                            .message(String.format("Registration with id: %s not found", id))
                            .build();
                });

        log.info("Registration found: {}", registration);
        return registration;
    }

    @Override
    public List<RegistrationCreateRequestDto> getAllByEventId(Long eventId, Pageable pageable) {
        log.info("Starting method getAllByEventId for eventId: {} with pageable: {}", eventId, pageable);

        Page<RegistrationProjection> allEventById = registrationRepository.findAllByEventId(eventId, pageable);

        if (Objects.isNull(allEventById) || allEventById.isEmpty()) {
            return Collections.emptyList();
        }

        List<RegistrationCreateRequestDto> dtoList = registrationMapper.toListDto(allEventById.getContent());
        log.info("Mapped registrations to DTO list. Total count: {}", dtoList.size());

        return dtoList;
    }

    @Override
    @Transactional
    public void deleteByPhoneNumberAndPassword(Long eventId, RegistrationDeleteRequestDto registrationDeleteRequestDto) {
        int number = registrationDeleteRequestDto.number();
        String password = registrationDeleteRequestDto.password();

        log.info("Starting method deleteByPhoneNumberAndPassword for number: {}", number);

        EventDto eventFromEventService = eventClient.getEventById(eventId);

        Registration registration = registrationRepository.findByNumberAndPassword(number, password)
                .orElseThrow(() -> NotFoundException.builder()
                        .message(String.format("No registrations found for deletion with number: %s and password %s", number, password))
                        .build());


        if (eventFromEventService.startDateTime().isAfter(LocalDateTime.now()) &&
                registration.getStatus().equals(Status.APPROVED)) {

            throw BadRequestException.builder()
                    .message("The event has already started and registration has been confirmed")
                    .build();
        }
        registrationRepository.deleteById(registration.getId());

        if (registration.getStatus() == Status.APPROVED) {
            eventPublisherService.publishRegistrationDeletedEvent(registration.getEventId());
        }
    }

    @Override
    @Transactional
    public RegistrationFullResponseDto updateRegistrationStatus(RegistrationStatusUpdateRequestDto request,
                                                                Long userId, Long id) {
        log.info("Starting method updateRegistrationStatus with id: {}, status: {}", id, request.status());


        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Registration not found for ID: {}", id);
                    return NotFoundException.builder()
                            .message(String.format("Registration with id: %s not found", id))
                            .build();
                });

        if (request.status() == Status.REJECTED && request.rejectionReason() == null) {
            throw BadRequestException.builder()
                    .message("Rejection reason is required for status REJECTED")
                    .build();
        }

        EventTeamDto eventTeam = eventClient.getEventTeam(registration.getEventId());

        Set<Long> managerIds = eventTeam.personnel().stream()
                .map(ManagerDto::userId)
                .collect(Collectors.toSet());

        if (!managerIds.contains(userId)) {
            throw BadRequestException.builder()
                    .message("You don't have enough rights to change the status of the registration")
                    .build();
        }


        registration.setStatus(request.status());
        registration.setRejectionReason(request.rejectionReason());
        registrationRepository.updateRegistrationById(registration);

        return registrationMapper.toFullResponseDto(registration);
    }

    @Override
    public List<RegistrationFullResponseDto> getRegistrationsByStatusAndEventId(Set<Status> statuses, Long eventId, Pageable pageable) {
        log.info("Service Method getRegistrationsByStatusAndEventId called with statuses: {}, eventId: {}, pageable: {}", statuses, eventId, pageable);

        Page<Registration> registrations = registrationRepository.findByStatusInAndEventId(statuses, eventId, pageable);

        if (Objects.isNull(registrations) || registrations.isEmpty()) {
            log.info("No registrations found for eventId: {} with statuses: {}", eventId, statuses);
            return Collections.emptyList();
        }

        List<RegistrationFullResponseDto> responses = registrationMapper.toFullResponseDtoList(registrations.getContent());
        log.info("Retrieved {} registrations for eventId: {}", responses.size(), eventId);
        return responses;
    }

    @Override
    public Map<Status, Integer> getStatusCounts(Long eventId) {
        log.info("Service Method getStatusCounts called for eventId: {}", eventId);

        Map<Status, Integer> statusCount = new HashMap<>();
        List<StatusProjection> statusProjections = registrationRepository.countByEventIdGroupByStatus(eventId);

        if (Objects.isNull(statusProjections) || statusProjections.isEmpty()) {
            log.info("No status counts found for eventId: {}", eventId);
            return Collections.emptyMap();
        }

        for (StatusProjection statusProjection : statusProjections) {
            statusCount.put(statusProjection.getStatus(), statusProjection.getCount());
        }

        log.info("Status counts for eventId {}: {}", eventId, statusCount);
        return statusCount;
    }

    @Override
    public StatusDto getStatusByEventIdAndUserId(Long eventId, Long userId) {
        log.info("Service Method getStatusByEventIdAndUserId called with eventId: {}, userId: {}", eventId, userId);

        UserResponseDTO user = userClient.findById(userId);
        log.debug("Retrieved user details: {}", user);

        return registrationRepository.findByEventIdAndEmail(eventId, user.email())
                .map(registration -> {
                    log.info("Found registration status: {} for userId: {} and eventId: {}", registration.status(), userId, eventId);
                    return new StatusDto(registration.status());
                })
                .orElseThrow(() -> {
                    String message = String.format("User with id: %s is not registered for event with id: %s", userId, eventId);
                    log.warn(message);
                    return NotFoundException.builder()
                            .message(message)
                            .build();
                });
    }

}