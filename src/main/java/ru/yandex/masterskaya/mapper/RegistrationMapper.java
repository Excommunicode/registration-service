package ru.yandex.masterskaya.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistrationMapper {

    Registration toModel(RegistrationCreateRequestDto registrationRequestDTO, String password);

    RegistrationResponseDTO toDto(Registration registration);

    Registration toModelAfterDto(RegistrationUpdateRequestDto registrationDto);

    RegistrationUpdateRequestDto toFullDto(Registration registration);

    List<RegistrationCreateRequestDto> toListDto(List<RegistrationProjection> registrationProjectionList);

    RegistrationFullResponseDto toFullResponseDto(Registration registration);
}