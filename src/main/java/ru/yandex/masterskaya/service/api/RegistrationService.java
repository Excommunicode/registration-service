package ru.yandex.masterskaya.service.api;

import org.springframework.data.domain.Pageable;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusCountResponseDto;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.model.Status;

import java.util.List;
import java.util.Set;

public interface RegistrationService {

    RegistrationResponseDTO addRegistration(RegistrationCreateRequestDto eventRegistrationResponseDTO);

    RegistrationUpdateRequestDto updateRegistration(RegistrationUpdateRequestDto eventRegistrationDto);

    RegistrationCreateRequestDto getRegistration(Long id);

    List<RegistrationCreateRequestDto> getAllByEventId(Long eventId, Pageable pageable);

    void deleteByPhoneNumberAndPassword(RegistrationDeleteRequestDto someDto);

    RegistrationFullResponseDto updateRegistrationStatus(RegistrationStatusUpdateRequestDto request);

    List<RegistrationFullResponseDto> getRegistrationsByStatusAndEventId(Set<Status> statuses, Long eventId);

    RegistrationStatusCountResponseDto getStatusCounts(Long eventId);
}
