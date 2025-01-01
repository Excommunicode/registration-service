package ru.yandex.masterskaya.dto;

import lombok.Builder;
import ru.yandex.masterskaya.enums.Status;

import java.io.Serializable;

@Builder(toBuilder = true)
public record StatusDto(Status status) implements Serializable {
}