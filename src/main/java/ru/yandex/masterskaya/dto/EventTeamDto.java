package ru.yandex.masterskaya.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder(toBuilder = true)
public record EventTeamDto(Long eventId,
                           List<ManagerDto> personnel) implements Serializable {
}