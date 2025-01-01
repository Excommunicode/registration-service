package ru.yandex.masterskaya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.masterskaya.constant.Constant;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.enums.Status;
import ru.yandex.masterskaya.service.contract.RegistrationService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/registrations")
@Tag(name = "Registration Controller", description = "Контроллер для управления регистрациями на мероприятия.")
public class RegistrationController {

    private final RegistrationService registrationService;


    @Operation(
            summary = "Добавить новую регистрацию",
            description = "Создает новую регистрацию на мероприятие с указанными данными. " +
                    "Необходимо предоставить имя пользователя, электронную почту, номер телефона и идентификатор мероприятия.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания регистрации",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegistrationCreateRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = "{ \"username\": \"IvanIvanov\", \"email\": \"ivan@example.com\", \"phone\": \"+1234567890\", \"eventId\": 1 }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Регистрация успешно создана.",
                            content = @Content(
                                    schema = @Schema(implementation = RegistrationResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Пример успешного ответа",
                                            value = "{ \"number\": \"1\",  \"password\": \"password\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации данных. Проверьте корректность введенной информации.",
                            content = @Content(
                                    schema = @Schema(
                                            example = "{ \"timestamp\": \"2024-12-30T12:34:56\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"Validation failed\", \"details\": [\"Username must be between 3 and 50 characters\"] }"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера. Попробуйте позже.",
                            content = @Content
                    )
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponseDTO addRegistration(
            @Valid @RequestBody
            @Parameter(
                    description = "Данные для регистрации",
                    required = true,
                    schema = @Schema(implementation = RegistrationCreateRequestDto.class)
            ) RegistrationCreateRequestDto registrationRequest) {
        log.info("Endpoint /registrations POST started. Received request to create registration {}", registrationRequest);
        return registrationService.addRegistration(registrationRequest);
    }


    @Operation(
            summary = "Обновить регистрацию",
            description = "Обновляет существующую регистрацию с указанным ID события",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные регистрации",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationUpdateRequestDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Регистрация успешно обновлена.",
                            content = @Content(schema = @Schema(implementation = RegistrationUpdateRequestDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Регистрация не найдена.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации данных.",
                            content = @Content
                    )
            }
    )
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public RegistrationUpdateRequestDto updateRegistration(
            @Valid @RequestBody @Parameter(description = "Обновленные данные регистрации.") RegistrationUpdateRequestDto eventRegistrationDto) {
        log.info("Endpoint /registrations PATCH started. Received request to update registration {} ", eventRegistrationDto);
        return registrationService.updateRegistration(eventRegistrationDto);
    }

    @Operation(
            summary = "Получить регистрацию",
            description = "Возвращает данные регистрации по указанному ID.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID регистрации.",
                            required = true,
                            example = "1",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Регистрация найдена.",
                            content = @Content(schema = @Schema(implementation = RegistrationCreateRequestDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Регистрация не найдена.",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RegistrationCreateRequestDto getRegistration(
            @PathVariable @Parameter(description = "ID регистрации.") Long id) {
        log.info("Endpoint /registrations/{id} GET started. Received request to get registration with id: {}", id);
        return registrationService.getRegistration(id);
    }

    @Operation(
            summary = "Получить все регистрации по ID мероприятия",
            description = "Возвращает список всех регистраций для указанного мероприятия с пагинацией.",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "ID мероприятия.",
                            required = true,
                            example = "10",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "page",
                            description = "Номер страницы для пагинации.",
                            example = "0",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "size",
                            description = "Размер страницы для пагинации.",
                            example = "20",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Параметры сортировки.",
                            example = "id,desc",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Регистрации успешно получены.",
                            content = @Content(schema = @Schema(implementation = RegistrationCreateRequestDto.class, type = "array"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные входные параметры.",
                            content = @Content
                    )
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RegistrationCreateRequestDto> getAllByEventId(
            @RequestParam @Parameter(description = "ID мероприятия.") Long eventId,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC)
            @Parameter(description = "Параметры пагинации.") Pageable pageable) {
        log.info("Endpoint /registrations GET started. Received request to get registrations with eventId: {}", eventId);
        return registrationService.getAllByEventId(eventId, pageable);
    }

    @Operation(
            summary = "Удалить регистрацию",
            description = "Удаляет регистрацию на мероприятие, используя номер телефона и пароль.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для удаления регистрации",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationDeleteRequestDto.class))
            ),
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "ID мероприятия.",
                            required = true,
                            example = "10",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Регистрация успешно удалена.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации данных.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Регистрация не найдена.",
                            content = @Content
                    )
            }
    )
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByPhoneAndPassword(
            @Valid @RequestBody RegistrationDeleteRequestDto someDto,
            @RequestParam @Parameter(description = "ID мероприятия.") Long eventId) {
        log.info("Endpoint /registrations DELETE started. Received request to delete registration with eventId: {}", eventId);
        registrationService.deleteByPhoneNumberAndPassword(eventId, someDto);
    }


    @Operation(
            summary = "Обновить статус регистрации",
            description = "Позволяет обновить статус регистрации по ее идентификатору.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Идентификатор регистрации",
                            required = true,
                            example = "1",
                            in = ParameterIn.PATH
                    ),
                    @Parameter(
                            name = Constant.X_USER_ID,
                            description = "Идентификатор пользователя",
                            required = true,
                            example = "123",
                            in = ParameterIn.HEADER
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Объект запроса для обновления статуса",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationStatusUpdateRequestDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Статус успешно обновлен",
                            content = @Content(schema = @Schema(implementation = RegistrationFullResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные входные данные",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Регистрация не найдена",
                            content = @Content
                    )
            }
    )
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public RegistrationFullResponseDto updateStatus(
            @PathVariable @Min(1) Long id,
            @RequestBody RegistrationStatusUpdateRequestDto request,
            @RequestHeader(Constant.X_USER_ID) Long userId) {
        log.info("Endpoint /registrations/{id}/status PATCH started. Received request to update status registration" +
                " with id: {} and RegistrationStatusUpdateRequestDto: {} and userId: {}", id, request, userId);
        return registrationService.updateRegistrationStatus(request, userId, id);
    }


    @Operation(
            summary = "Получить регистрации по статусам и идентификатору события",
            description = "Возвращает список регистраций, соответствующих заданным статусам и идентификатору события.",
            parameters = {
                    @Parameter(
                            name = "statuses",
                            description = "Набор статусов для фильтрации",
                            required = true,
                            example = "[\"ACTIVE\", \"PENDING\"]",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "eventId",
                            description = "Идентификатор события",
                            required = true,
                            example = "10",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "page",
                            description = "Номер страницы для пагинации",
                            example = "0",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "size",
                            description = "Размер страницы для пагинации",
                            example = "20",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Параметры сортировки",
                            example = "createdDateTime,ASC",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список регистраций успешно получен",
                            content = @Content(schema = @Schema(implementation = RegistrationFullResponseDto.class, type = "array"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные входные параметры",
                            content = @Content
                    )
            }
    )
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<RegistrationFullResponseDto> getByStatusAndEventId(
            @RequestParam Set<Status> statuses,
            @RequestParam Long eventId,
            @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Endpoint /registrations/status GET started. With parameters statuses: {}, eventId: {}, pageable {}",
                statuses, eventId, pageable);
        return registrationService.getRegistrationsByStatusAndEventId(statuses, eventId, pageable);
    }


    @Operation(
            summary = "Получить количество регистраций по статусам для события",
            description = "Возвращает количество регистраций для каждого статуса по заданному идентификатору события.",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "Идентификатор события",
                            required = true,
                            example = "10",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Количество регистраций по статусам успешно получено",
                            content = @Content(schema = @Schema(implementation = Map.class,
                                    description = "Карта статусов и их количества"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Событие не найдено",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{eventId}/status/counts")
    public Map<Status, Integer> getStatusCounts(@PathVariable Long eventId) {
        log.info("Endpoint /registrations/{eventId}/status/counts GET started. With parameters eventId: {}", eventId);
        return registrationService.getStatusCounts(eventId);
    }


    @Operation(
            summary = "Получить статус регистрации по идентификаторам события и пользователя",
            description = "Возвращает статус регистрации для заданных идентификаторов события и пользователя.",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "Идентификатор события",
                            required = true,
                            example = "10",
                            in = ParameterIn.PATH
                    ),
                    @Parameter(
                            name = "userId",
                            description = "Идентификатор пользователя",
                            required = true,
                            example = "123",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Статус регистрации успешно получен",
                            content = @Content(schema = @Schema(implementation = StatusDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Регистрация не найдена",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{eventId}/status/{userId}")
    public StatusDto getStatusByEventIdAndUserId(@PathVariable Long eventId, @PathVariable Long userId) {
        log.info("Endpoint /{eventId}/status/{userid} GET started. " +
                "Received request to get status with eventId: {} and userId: {}", eventId, userId);
        return registrationService.getStatusByEventIdAndUserId(eventId, userId);
    }
}