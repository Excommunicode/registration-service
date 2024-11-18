package ru.yandex.masterskaya.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.yandex.masterskaya.dto.EventRegistrationDto;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistrationMapper {

    Registration toModel(EventRegistrationRequestDTO eventRegistrationRequestDTO, String password);

    EventRegistrationResponseDTO toDto(Registration registration);

    Registration toModelAfterDto(EventRegistrationDto eventRegistrationDto);

    EventRegistrationDto toFullDto(Registration registration);

    List<EventRegistrationRequestDTO> toListDto(List<RegistrationProjection> registrationProjectionList);
}