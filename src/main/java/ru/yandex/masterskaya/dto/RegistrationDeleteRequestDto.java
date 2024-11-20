package ru.yandex.masterskaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RegistrationDeleteRequestDto {
    private String phone;
    private String password;
}
