package ru.yandex.masterskaya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusCountResponseDto;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.exception.BadRequestException;
import ru.yandex.masterskaya.exception.NotFoundException;
import ru.yandex.masterskaya.mapper.RegistrationMapper;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.model.Status;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;


    @Override
    @Transactional
    public RegistrationResponseDTO addRegistration(RegistrationCreateRequestDto registrationCreateRequestDto) {
        log.info("Starting method addRegistration. Received registrationCreateRequestDto: {}", registrationCreateRequestDto);

        String password = UUID.randomUUID().toString().substring(0, 4);


        Registration registration = registrationMapper.toModel(registrationCreateRequestDto, password);
        registration.setCreatedDateTime(LocalDateTime.now());

        Registration savedRegistration = registrationRepository.saveAndReturn(registration, registration.getEventId());
        log.info("Registration successfully saved with ID: {} and details: {}", savedRegistration.getId(), savedRegistration);

        return registrationMapper.toDto(savedRegistration);
    }

    @Override
    @Transactional
    public RegistrationUpdateRequestDto updateRegistration(RegistrationUpdateRequestDto registrationUpdateRequestDto) {
        log.info("Starting method updateRegistration for eventId:  with data: {}", registrationUpdateRequestDto);

        Registration registration = registrationMapper.toModelAfterDto(registrationUpdateRequestDto);


        Registration updatedRegistration = registrationRepository.updateByEventIdAndNumberAndPassword(
                registration.getNumber(),
                registration.getPassword(),
                registration.getUsername(),
                registration.getEmail(),
                registration.getPhone()
        ).orElseThrow(() -> BadRequestException.builder()
                .message(String.format("This registration: %s was not possible to update the registration", registrationUpdateRequestDto))
                .build());


        log.info("Registration successfully updated. Updated details: {}", updatedRegistration);

        return registrationMapper.toFullDto(updatedRegistration);
    }

    @Override
    public RegistrationCreateRequestDto getRegistration(Long id) {
        log.info("Starting method getRegistration with ID: {}", id);

        RegistrationCreateRequestDto registration = registrationRepository.findByIdDTO(id)
                .orElseThrow(() -> {
                    log.warn("Registration not found for ID: {}", id);
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

        List<RegistrationProjection> allByEventId = registrationRepository.findAllByEventId(eventId, pageable);

        if (allByEventId == null || allByEventId.isEmpty()) {
            return Collections.emptyList();
        }

        List<RegistrationCreateRequestDto> dtoList = registrationMapper.toListDto(allByEventId);
        log.info("Mapped registrations to DTO list. Total count: {}", dtoList.size());

        return dtoList;
    }

    @Override
    @Transactional
    public void deleteByPhoneNumberAndPassword(RegistrationDeleteRequestDto registrationDeleteRequestDto) {
        int number = registrationDeleteRequestDto.getNumber();
        String password = registrationDeleteRequestDto.getPassword();
        log.info("Starting method deleteByPhoneNumberAndPassword for number: {}", number);
        Registration registration = registrationRepository.findByNumberAndPassword(number, password)
                .orElseThrow(() -> NotFoundException.builder()
                        .message(String.format("No registrations found for deletion with number: %s and password %s", number, password))
                        .build());
        registrationRepository.deleteByPhoneAndPassword(number, password);

        if (registration.getStatus() == Status.APPROVED) {
            handleWaitListUpdate(registration.getEventId());
        }
    }

    @Transactional
    public RegistrationFullResponseDto updateRegistrationStatus(RegistrationStatusUpdateRequestDto request, Long id) {
        log.info("Starting method updateRegistrationStatus with id: {}, status: {}", id, request.getStatus());
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Registration not found for ID: {}", id);
                    return NotFoundException.builder()
                            .message(String.format("Registration with id: %s not found", id))
                            .build();
                });

        if (request.getStatus() == Status.REJECTED && request.getRejectionReason() == null) {
            throw BadRequestException.builder()
                    .message("Rejection reason is required for status REJECTED")
                    .build();
        }

        registration.setStatus(request.getStatus());
        registration.setRejectionReason(request.getRejectionReason());
        registrationRepository.save(registration);

        return registrationMapper.toFullResponseDto(registration);
    }

    public List<RegistrationFullResponseDto> getRegistrationsByStatusAndEventId(Set<Status> statuses, Long eventId) {
        return registrationRepository.findByStatusInAndEventIdOrderByCreatedDateTimeAsc(statuses, eventId).stream()
                .map(registrationMapper::toFullResponseDto)
                .collect(Collectors.toList());
    }

    public RegistrationStatusCountResponseDto getStatusCounts(Long eventId) {
        List<Object[]> results = registrationRepository.countByEventIdGroupByStatus(eventId);
        RegistrationStatusCountResponseDto statusCounts = new RegistrationStatusCountResponseDto(1L, new HashMap<>());
        for (Object[] result : results) {
            statusCounts.getStatusCounts().put((Status) result[0], (Long) result[1]);
        }
        return statusCounts;
    }

    private void handleWaitListUpdate(Long eventId) {
        Optional<Registration> waitListCandidate = registrationRepository
                .findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc(eventId, Status.WAITLIST);

        waitListCandidate.ifPresent(candidate -> {
            log.info(String.valueOf(candidate));
            candidate.setStatus(Status.PENDING);
            registrationRepository.save(candidate);
        });
    }
}