package ru.yandex.masterskaya.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record EventDto(Long id,
                       String name,
                       String description,
                       LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       String location,
                       Long ownerId) implements Serializable {
}