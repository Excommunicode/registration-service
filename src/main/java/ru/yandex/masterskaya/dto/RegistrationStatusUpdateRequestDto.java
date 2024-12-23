package ru.yandex.masterskaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.masterskaya.model.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RegistrationStatusUpdateRequestDto {
    private Status status;
    private String rejectionReason;
}
