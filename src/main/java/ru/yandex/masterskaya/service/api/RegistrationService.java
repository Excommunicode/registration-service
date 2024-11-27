package ru.yandex.masterskaya.service.api;

import org.springframework.data.domain.Pageable;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;

import java.util.List;

public interface RegistrationService {

    RegistrationResponseDTO addRegistration(RegistrationCreateRequestDto eventRegistrationResponseDTO);

    RegistrationUpdateRequestDto updateRegistration(RegistrationUpdateRequestDto eventRegistrationDto);

    RegistrationCreateRequestDto getRegistration(Long id);

    List<RegistrationCreateRequestDto> getAllByEventId(Long eventId, Pageable pageable);

    void deleteByPhoneNumberAndPassword(RegistrationDeleteRequestDto someDto);

}
