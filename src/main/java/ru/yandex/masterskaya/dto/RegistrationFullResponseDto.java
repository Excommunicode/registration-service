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
public class RegistrationFullResponseDto {
    private Long id;

    private String username;

    private String email;

    private String phone;

    private Long eventId;

    private Status status;

    private String rejectionReason;
}
