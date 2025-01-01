package ru.yandex.masterskaya.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RegistrationMapper {

    @Mappings({
            @Mapping(target = "number", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "rejectionReason", ignore = true),
            @Mapping(target = "createdDateTime", ignore = true)
    })
    Registration toModel(RegistrationCreateRequestDto registrationRequestDTO, String password);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "eventId", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "rejectionReason", ignore = true),
            @Mapping(target = "createdDateTime", ignore = true)
    })
    Registration toModelAfterDto(RegistrationUpdateRequestDto registrationDto);

    RegistrationResponseDTO toDto(Registration registration);

    RegistrationUpdateRequestDto toFullDto(Registration registration);

    List<RegistrationCreateRequestDto> toListDto(List<RegistrationProjection> registrationProjectionList);

    RegistrationFullResponseDto toFullResponseDto(Registration registration);

    List<RegistrationFullResponseDto> toFullResponseDtoList(List<Registration> registrations);
}