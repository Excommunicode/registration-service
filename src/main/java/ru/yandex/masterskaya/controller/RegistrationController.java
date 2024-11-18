package ru.yandex.masterskaya.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.masterskaya.dto.EventRegistrationDto;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventRegistrationResponseDTO addRegistration(@Valid @RequestBody EventRegistrationRequestDTO eventRegistrationResponseDTO) {
        log.info("Endpoint /registrations POST started. Received request to create registration {}", eventRegistrationResponseDTO);
        return registrationService.addRegistration(eventRegistrationResponseDTO);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventRegistrationDto updateRegistration(@PathVariable Long eventId, @Valid @RequestBody EventRegistrationDto eventRegistrationDto) {
        log.info("Endpoint /registrations/{eventId} PATCH started. Received request to update registration {} with id:{}", eventRegistrationDto, eventId);
        return registrationService.updateRegistration(eventId, eventRegistrationDto);
    }

    @GetMapping("/{id}")
    public EventRegistrationRequestDTO getRegistration(@PathVariable Long id) {
        log.info("Endpoint /registrations/{id} GET started. Received request to get registration with id:{}", id);
        return registrationService.getRegistration(id);
    }

    @GetMapping
    public List<EventRegistrationRequestDTO> getAllByEventId(@RequestParam Long eventId,
                                                             @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
                                                             Pageable pageable) {
        log.info("Endpoint /registrations GET started. Received request to get registration with eventId:{}", eventId);
        return registrationService.getAllByEventId(eventId, pageable);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByPhoneAndPassword(@RequestParam String phone, @RequestParam String password) {
        log.info("Endpoint /registrations/ DELETE started. Received request to Delete registration with same parameters");
        String replace = phone.replace(' ', '+');
        registrationService.deleteByPhoneNumberAndPassword(replace, password);
    }
}
