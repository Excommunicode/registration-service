package ru.yandex.masterskaya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterskaya.dto.EventRegistrationDto;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;
import ru.yandex.masterskaya.exception.NotFoundException;
import ru.yandex.masterskaya.mapper.RegistrationMapper;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;


    @Override
    @Transactional
    public EventRegistrationResponseDTO addRegistration(EventRegistrationRequestDTO eventRegistrationRequestDTO) {
        log.info("Starting adding eventRegistrationRequestDTO: {}", eventRegistrationRequestDTO);


        String substringUuid = UUID.randomUUID().toString().substring(0, 4);

        Registration registration = registrationMapper.toModel(eventRegistrationRequestDTO, substringUuid);


        Registration savedRegistration = registrationRepository.saveAndReturn(registration, registration.getEventId());

        log.info("Registration successfully created {}", savedRegistration);
        return registrationMapper.toDto(savedRegistration);
    }

    @Override
    @Transactional
    public EventRegistrationDto updateRegistration(Long eventId, EventRegistrationDto eventRegistrationDto) {
        log.info("Starting updating eventRegistration {} with eventId: {}", eventRegistrationDto, eventId);
        Registration registration = registrationMapper.toModelAfterDto(eventRegistrationDto);

        Registration updateByEventIdAndNumberAndPassword = registrationRepository.updateByEventIdAndNumberAndPassword(eventId, registration.getNumber(), registration.getPassword(),
                registration.getUsername(), registration.getEmail(), registration.getPhone());

        log.info("Registration successfully updated {}", updateByEventIdAndNumberAndPassword);
        return registrationMapper.toFullDto(updateByEventIdAndNumberAndPassword);
    }

    @Override
    public EventRegistrationRequestDTO getRegistration(Long id) {
        return registrationRepository.findByIdDTO(id).orElseThrow(() -> NotFoundException.builder()
                .message(String.format("Registration with id: %s not found", id))
                .build());
    }

    @Override
    public List<EventRegistrationRequestDTO> getAllByEventId(Long eventId, Pageable pageable) {
        List<RegistrationProjection> allByEventId = registrationRepository.findAllByEventId(eventId, pageable);
        return registrationMapper.toListDto(allByEventId);
    }

    @Override
    @Transactional
    public void deleteByPhoneNumberAndPassword(String number, String password) {
        registrationRepository.deleteByPhoneAndPassword(number, password);
    }
}