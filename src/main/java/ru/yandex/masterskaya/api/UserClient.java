package ru.yandex.masterskaya.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.masterskaya.config.FeignConfig;
import ru.yandex.masterskaya.dto.UserResponseDTO;

@FeignClient(name = "user-service", url = "${user-service.url}", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponseDTO findById(@PathVariable Long id);

    @GetMapping("/users/email")
    UserResponseDTO findByEmail(@RequestParam String email);
}