package ru.yandex.masterskaya.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.yandex.masterskaya.dto.EventDto;

import java.util.Optional;

@FeignClient(name = "event-service", url = "${event-service.url}")
public interface EventClient {

    @RequestMapping(method = RequestMethod.GET, value = "/events/{id}", consumes = "application/json")
    Optional<EventDto> getEventById(@PathVariable Long id);
}