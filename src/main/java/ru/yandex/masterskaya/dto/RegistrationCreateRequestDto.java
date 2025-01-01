package ru.yandex.masterskaya.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;


@Builder
public record RegistrationCreateRequestDto(Long id,
                                           @NotNull(message = "Username cannot be null")
                                           @NotBlank(message = "Username cannot be blank")
                                           @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,
                                           @NotNull(message = "Email cannot be null")
                                           @NotBlank(message = "Email cannot be blank") String email,
                                           @NotNull(message = "Phone number cannot be null")
                                           @NotBlank(message = "Phone number cannot be blank")
                                           @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be a valid international format, e.g., +123456789")
                                           String phone,
                                           Long eventId) implements Serializable {
}
