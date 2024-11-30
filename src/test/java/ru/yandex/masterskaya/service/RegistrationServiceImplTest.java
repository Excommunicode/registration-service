package ru.yandex.masterskaya.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.Status;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RegistrationServiceImplTest {

    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;
    private final EntityManager entityManager;


    @AfterEach
    void clean() {
        registrationRepository.deleteAll();
    }


    @Test
    @DisplayName("Добавление регистрации")
    void addRegistration() {
        RegistrationCreateRequestDto eventRegistrationRequestDTO = Instancio.of(RegistrationCreateRequestDto.class)
                .ignore(Select.field(RegistrationCreateRequestDto::getId))
                .create();

        RegistrationResponseDTO addedRegistration = registrationService.addRegistration(eventRegistrationRequestDTO);

        assertNotNull(addedRegistration);
        assertEquals(addedRegistration.getNumber(), 1);
        assertNotNull(addedRegistration.getPassword());
    }

    @Test
    @DisplayName("Обновление регистрации")
    void updateRegistration() {
        RegistrationCreateRequestDto eventRegistrationRequestDTO = Instancio.of(RegistrationCreateRequestDto.class)
                .ignore(Select.field(RegistrationCreateRequestDto::getId))
                .set(Select.field(RegistrationCreateRequestDto::getEventId), 1L)
                .create();

        RegistrationResponseDTO addedRegistration = registrationService.addRegistration(eventRegistrationRequestDTO);

        RegistrationUpdateRequestDto eventRegistrationDto = RegistrationUpdateRequestDto.builder()
                .username("Leonid")
                .email("Leonid@mail.ru")
                .phone("+23433254")
                .number(addedRegistration.getNumber())
                .password(addedRegistration.getPassword())
                .build();

        RegistrationUpdateRequestDto updateRegistration = registrationService.updateRegistration(eventRegistrationDto);
        assertNotNull(updateRegistration);
        assertEquals(updateRegistration.getNumber(), addedRegistration.getNumber());
        assertEquals(updateRegistration.getPassword(), addedRegistration.getPassword());
    }

    @Test
    @DisplayName("Получение регистрации па айди")
    void getRegistration() {
        RegistrationCreateRequestDto eventRegistrationRequestDTO = Instancio.of(RegistrationCreateRequestDto.class)
                .ignore(Select.field(RegistrationCreateRequestDto::getId))
                .set(Select.field(RegistrationCreateRequestDto::getEventId), 1L)
                .create();

        registrationService.addRegistration(eventRegistrationRequestDTO);

        RegistrationCreateRequestDto registration = registrationService.getRegistration(1L);


        assertEquals(eventRegistrationRequestDTO.getUsername(), registration.getUsername());
        assertEquals(eventRegistrationRequestDTO.getEmail(), registration.getEmail());
        assertEquals(eventRegistrationRequestDTO.getPhone(), registration.getPhone());
        assertEquals(eventRegistrationRequestDTO.getEventId(), registration.getEventId());
    }

    @Test
    @DisplayName("Получение регистрации по евенту с пагинацией")
    void getAllByEventId() {

        List<Registration> eventRegistrationRequestDTOS = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), 1L)
                        .create())
                .limit(10)
                .toList();

        List<Registration> registrations = registrationRepository.saveAll(eventRegistrationRequestDTOS);

        assertEquals(10, registrations.size(), "Expected 10 registrations to be saved");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        List<RegistrationCreateRequestDto> allByEventId = registrationService.getAllByEventId(1L, pageable);

        assertEquals(10, allByEventId.size(), "Expected 10 registrations to be returned for event ID 1");

        for (RegistrationCreateRequestDto dto : allByEventId) {
            assertEquals(1L, dto.getEventId(), "Event ID should be 1 for all registrations");
        }

        for (int i = 0; i < allByEventId.size() - 1; i++) {
            assertTrue(allByEventId.get(i).getId() > allByEventId.get(i + 1).getId(),
                    "Registrations should be sorted in descending order by id");
        }
    }

    @Test
    @DisplayName("Удаление регистрации")
    void deleteByPhoneNumberAndPassword() {
        RegistrationCreateRequestDto eventRegistrationRequestDTO = Instancio.of(RegistrationCreateRequestDto.class)
                .ignore(Select.field(RegistrationCreateRequestDto::getId))
                .set(Select.field(RegistrationCreateRequestDto::getEventId), 1L)
                .create();


        RegistrationResponseDTO eventRegistrationResponseDTO = registrationService.addRegistration(eventRegistrationRequestDTO);

        RegistrationDeleteRequestDto someDto = RegistrationDeleteRequestDto.builder()
                .number(eventRegistrationResponseDTO.getNumber())
                .password(eventRegistrationResponseDTO.getPassword())
                .build();


        registrationRepository.findById(1L).orElse(null);

        entityManager.clear();

        registrationService.deleteByPhoneNumberAndPassword(someDto);

        entityManager.clear();

        assertFalse(registrationRepository.existsById(1L), "Запись не была удалена");
    }

    @Test
    void updateRegistrationStatus() {
        RegistrationStatusUpdateRequestDto registrationStatusUpdateRequestDto =
                new RegistrationStatusUpdateRequestDto(Status.REJECTED, "reason");

        RegistrationCreateRequestDto eventRegistrationRequestDTO = RegistrationCreateRequestDto.builder()
                .eventId(30L)
                .phone("+12344234")
                .username("username")
                .email("email@gmail.com")
                .build();

        registrationService.addRegistration(eventRegistrationRequestDTO);

        RegistrationFullResponseDto response = registrationService.updateRegistrationStatus(
                registrationStatusUpdateRequestDto,
                registrationService.getAllByEventId(30L, Pageable.ofSize(1)).getFirst().getId());

        assertEquals(Status.REJECTED, response.getStatus());
        assertEquals(registrationStatusUpdateRequestDto.getRejectionReason(), response.getRejectionReason());
    }

    @Test
    void getRegistrationsByStatusAndEventId() {

        initFiveRegistrationsWithStatuses(31L);

        List<RegistrationFullResponseDto> pending = registrationService.getRegistrationsByStatusAndEventId(Set.of(Status.PENDING), 31L);
        List<RegistrationFullResponseDto> waitlist = registrationService.getRegistrationsByStatusAndEventId(Set.of(Status.WAITLIST), 31L);
        List<RegistrationFullResponseDto> rejected = registrationService.getRegistrationsByStatusAndEventId(Set.of(Status.REJECTED), 31L);
        List<RegistrationFullResponseDto> approved = registrationService.getRegistrationsByStatusAndEventId(Set.of(Status.APPROVED), 31L);


        assertEquals(1, pending.size());
        assertEquals(1, waitlist.size());
        assertEquals(2, rejected.size());
        assertEquals(1, approved.size());
    }

    @Test
    void getStatusCounts() {

        initFiveRegistrationsWithStatuses(32L);

        Map<Status, Long> counts = registrationService.getStatusCounts(32L).getStatusCounts();

        assertEquals(1, counts.get(Status.APPROVED));
        assertEquals(1, counts.get(Status.WAITLIST));
        assertEquals(2, counts.get(Status.REJECTED));
        assertEquals(1, counts.get(Status.PENDING));

    }


    private void initFiveRegistrationsWithStatuses(Long eventId) {
        RegistrationCreateRequestDto eventRegistrationRequestDTO = RegistrationCreateRequestDto.builder()
                .eventId(eventId)
                .phone("+12344234")
                .username("username")
                .email("email@gmail.com")
                .build();

        for (int i = 0; i < 5; i++) {
            registrationService.addRegistration(eventRegistrationRequestDTO);
        }

        List<Long> ids = registrationService.getAllByEventId(eventId, Pageable.ofSize(5)).stream()
                .map(RegistrationCreateRequestDto::getId)
                .toList();

        RegistrationStatusUpdateRequestDto registrationStatusUpdateRequestDto =
                new RegistrationStatusUpdateRequestDto(Status.APPROVED, "reason");

        registrationService.updateRegistrationStatus(registrationStatusUpdateRequestDto, ids.get(0));

        registrationStatusUpdateRequestDto.setStatus(Status.WAITLIST);
        registrationService.updateRegistrationStatus(registrationStatusUpdateRequestDto, ids.get(1));

        registrationStatusUpdateRequestDto.setStatus(Status.REJECTED);
        registrationService.updateRegistrationStatus(registrationStatusUpdateRequestDto, ids.get(2));

        registrationStatusUpdateRequestDto.setStatus(Status.REJECTED);
        registrationService.updateRegistrationStatus(registrationStatusUpdateRequestDto, ids.get(3));
    }
}