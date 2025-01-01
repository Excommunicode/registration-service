package ru.yandex.masterskaya.dto;

import lombok.Builder;
import ru.yandex.masterskaya.enums.ManagerRole;

import java.io.Serializable;

@Builder(toBuilder = true)
public record ManagerDto(Long userId,
                         ManagerRole managerRole) implements Serializable {
}