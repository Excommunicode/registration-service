package ru.yandex.masterskaya.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.masterskaya.model.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatusUpdateRequestDto {
    @Min(1)
    private Long id;
    private Status status;
    private String rejectionReason;
}
