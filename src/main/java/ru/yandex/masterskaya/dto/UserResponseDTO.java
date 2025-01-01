package ru.yandex.masterskaya.dto;

import lombok.Builder;


@Builder(toBuilder = true)
public record UserResponseDTO(Long id,
                              String name,
                              String email,
                              String aboutMe) {
}
