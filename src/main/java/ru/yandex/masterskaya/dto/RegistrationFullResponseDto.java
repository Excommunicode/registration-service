package ru.yandex.masterskaya.dto;

import lombok.Builder;
import ru.yandex.masterskaya.enums.Status;

import java.io.Serializable;

@Builder(toBuilder = true)
public record RegistrationFullResponseDto(Long id,
                                          String username,
                                          String email,
                                          String phone,
                                          Long eventId,
                                          Status status,
                                          String rejectionReason) implements Serializable {
}