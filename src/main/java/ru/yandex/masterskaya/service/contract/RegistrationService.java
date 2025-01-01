package ru.yandex.masterskaya.service.contract;

import org.springframework.data.domain.Pageable;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.enums.Status;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RegistrationService {

    RegistrationResponseDTO addRegistration(RegistrationCreateRequestDto eventRegistrationResponseDTO);

    RegistrationUpdateRequestDto updateRegistration(RegistrationUpdateRequestDto eventRegistrationDto);

    RegistrationCreateRequestDto getRegistration(Long id);

    List<RegistrationCreateRequestDto> getAllByEventId(Long eventId, Pageable pageable);

    void deleteByPhoneNumberAndPassword(Long eventId, RegistrationDeleteRequestDto someDto);

    RegistrationFullResponseDto updateRegistrationStatus(RegistrationStatusUpdateRequestDto request, Long userId, Long id);

    List<RegistrationFullResponseDto> getRegistrationsByStatusAndEventId(Set<Status> statuses, Long eventId, Pageable pageable);

    Map<Status, Integer> getStatusCounts(Long eventId);

    StatusDto getStatusByEventIdAndUserId(Long eventId, Long userid);
}
