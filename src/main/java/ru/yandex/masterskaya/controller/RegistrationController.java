package ru.yandex.masterskaya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Registration Controller", description = "Контроллер для управления регистрациями на мероприятия.")
public class RegistrationController {

    private final RegistrationService registrationService;


    @Operation(
            summary = "Добавить новую регистрацию",
            description = "Создает новую регистрацию на мероприятие с указанными данными.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Регистрация успешно создана."),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных.")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventRegistrationResponseDTO addRegistration(
            @Valid @RequestBody @Parameter(description = "Данные для регистрации.") EventRegistrationRequestDTO eventRegistrationResponseDTO) {
        log.info("Endpoint /registrations POST started. Received request to create registration {}", eventRegistrationResponseDTO);
        return registrationService.addRegistration(eventRegistrationResponseDTO);
    }


    @Operation(
            summary = "Обновить регистрацию",
            description = "Обновляет существующую регистрацию с указанным ID события.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Регистрация успешно обновлена."),
                    @ApiResponse(responseCode = "404", description = "Регистрация не найдена.")
            }
    )
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventRegistrationDto updateRegistration(
            @PathVariable @Parameter(description = "ID события для обновления.") Long eventId,
            @Valid @RequestBody @Parameter(description = "Обновленные данные регистрации.") EventRegistrationDto eventRegistrationDto) {
        log.info("Endpoint /registrations/{eventId} PATCH started. Received request to update registration {} with id:{}", eventRegistrationDto, eventId);
        return registrationService.updateRegistration(eventId, eventRegistrationDto);
    }


    @Operation(
            summary = "Получить регистрацию",
            description = "Возвращает данные регистрации по указанному ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Регистрация найдена."),
                    @ApiResponse(responseCode = "404", description = "Регистрация не найдена.")
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventRegistrationRequestDTO getRegistration(@PathVariable @Parameter(description = "ID регистрации.") Long id) {
        log.info("Endpoint /registrations/{id} GET started. Received request to get registration with id:{}", id);
        return registrationService.getRegistration(id);
    }


    @Operation(
            summary = "Получить все регистрации по ID мероприятия",
            description = "Возвращает список всех регистраций для указанного мероприятия с пагинацией.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Регистрации успешно получены.")
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventRegistrationRequestDTO> getAllByEventId(
            @RequestParam @Parameter(description = "ID мероприятия.") Long eventId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            @Parameter(description = "Параметры пагинации.") Pageable pageable) {
        log.info("Endpoint /registrations GET started. Received request to get registration with eventId:{}", eventId);
        return registrationService.getAllByEventId(eventId, pageable);
    }


    @Operation(
            summary = "Удалить регистрацию",
            description = "Удаляет регистрацию на мероприятие, используя номер телефона и пароль.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Регистрация успешно удалена."),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных."),
                    @ApiResponse(responseCode = "404", description = "Регистрация не найдена.")
            }
    )
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByPhoneAndPassword(
            @RequestParam @Parameter(description = "Номер телефона.") String phone,
            @RequestParam @Parameter(description = "Пароль.") String password) {
        log.info("Endpoint /registrations/ DELETE started. Received request to Delete registration with same parameters");
        String replace = phone.replace(' ', '+');
        registrationService.deleteByPhoneNumberAndPassword(replace, password);
    }
}

