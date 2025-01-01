package ru.yandex.masterskaya.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record RegistrationDeleteRequestDto(int number,
                                           String password) {

}