package ru.yandex.masterskaya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.masterskaya.model.Status;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatusCountResponseDto {
    private Long eventId;
    private Map<Status, Long> statusCounts;
}