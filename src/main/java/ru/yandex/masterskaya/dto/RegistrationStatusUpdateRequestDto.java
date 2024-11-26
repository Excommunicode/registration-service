package ru.yandex.masterskaya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.masterskaya.model.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatusUpdateRequestDto {
    private Status status;
    private String rejectionReason;
}
