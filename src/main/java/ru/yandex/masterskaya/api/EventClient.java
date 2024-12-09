package ru.yandex.masterskaya.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.masterskaya.config.FeignConfig;
import ru.yandex.masterskaya.dto.EventDto;
import ru.yandex.masterskaya.dto.EventTeamDto;

@FeignClient(name = "event-service",  configuration = FeignConfig.class)
public interface EventClient {

    @GetMapping("/events/{id}")
    EventDto getEventById(@PathVariable Long id);

    @GetMapping("/managers/{eventId}")
    EventTeamDto getEventTeam(@PathVariable Long eventId);
}