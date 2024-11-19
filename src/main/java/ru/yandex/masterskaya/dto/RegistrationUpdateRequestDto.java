package ru.yandex.masterskaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RegistrationUpdateRequestDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer number;
    private String password;
}
