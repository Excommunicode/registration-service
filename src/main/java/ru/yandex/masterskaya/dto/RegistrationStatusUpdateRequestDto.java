package ru.yandex.masterskaya.dto;

import lombok.Builder;
import ru.yandex.masterskaya.enums.Status;


@Builder(toBuilder = true)
public record RegistrationStatusUpdateRequestDto(Status status,
                                                 String rejectionReason) {
}