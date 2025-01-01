package ru.yandex.masterskaya.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder(toBuilder = true)
public record RegistrationResponseDTO(Integer number,
                                      String password) implements Serializable {

}