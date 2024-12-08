package ru.yandex.masterskaya.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserResponseDTO {
    private Long id;
    private String name;

    @JsonProperty("e_mail")
    private String email;

    @JsonProperty("about_me")
    private String aboutMe;
}
