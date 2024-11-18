package ru.yandex.masterskaya.service.api;

import org.springframework.data.domain.Pageable;
import ru.yandex.masterskaya.dto.EventRegistrationDto;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;

import java.util.List;

public interface RegistrationService {

    EventRegistrationResponseDTO addRegistration(EventRegistrationRequestDTO eventRegistrationResponseDTO);

    EventRegistrationDto updateRegistration(Long eventId, EventRegistrationDto eventRegistrationDto);

    EventRegistrationRequestDTO getRegistration(Long id);

    List<EventRegistrationRequestDTO> getAllByEventId(Long eventId, Pageable pageable);

    void deleteByPhoneNumberAndPassword(String number, String password);
}
