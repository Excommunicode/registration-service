package ru.yandex.masterskaya.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record RegistrationDeletedEvent(Long eventId) {
}