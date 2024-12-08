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
import ru.yandex.masterskaya.dto.RegistrationFullResponseDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.Status;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.contract.RegistrationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("Обновление регистрации")
    void updateRegistration() {
        Registration registration = saveRegistration();

        RegistrationUpdateRequestDto eventRegistrationDto = RegistrationUpdateRequestDto.builder()
                .username("Leonid")
                .email("Leonid@mail.ru")
                .phone("+23433254")
                .number(registration.getNumber())
                .password(registration.getPassword())
                .build();

        RegistrationUpdateRequestDto updateRegistration = registrationService.updateRegistration(eventRegistrationDto);
        assertNotNull(updateRegistration);
        assertEquals(updateRegistration.getNumber(), registration.getNumber());
        assertEquals(updateRegistration.getPassword(), registration.getPassword());
    }

    @Test
    @DisplayName("Получение регистрации па айди")
    void getRegistration() {
        Registration registration1 = saveRegistration();

        RegistrationCreateRequestDto registration = registrationService.getRegistration(1L);


        assertEquals(registration1.getUsername(), registration.getUsername());
        assertEquals(registration1.getEmail(), registration.getEmail());
        assertEquals(registration1.getPhone(), registration.getPhone());
        assertEquals(registration1.getEventId(), registration.getEventId());
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
    void getRegistrationsByStatusAndEventId() {
        List<Registration> eventRegistrationRequestDTOS = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), 1L)
                        .set(Select.field(Registration::getStatus), Status.APPROVED)
                        .create())
                .limit(10)
                .toList();
        System.out.println();
        List<Registration> registrations = registrationRepository.saveAll(eventRegistrationRequestDTOS);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDateTime"));
        Set<Status> approved = Set.of(Status.APPROVED);

        List<RegistrationFullResponseDto> fullResponseDtos = registrationService.getRegistrationsByStatusAndEventId(approved, 1L, pageable);
        assertEquals(fullResponseDtos.size(), 10);
    }

    @Test
    void getStatusCounts() {

        saveRegistrationsStatus(Status.APPROVED);
        saveRegistrationsStatus(Status.PENDING);
        saveRegistrationsStatus(Status.REJECTED);
        saveRegistrationsStatus(Status.WAITLIST);

        Map<Status, Integer> statusCounts = registrationService.getStatusCounts(1L);

        assertEquals(10, statusCounts.get(Status.APPROVED));
        assertEquals(10, statusCounts.get(Status.PENDING));
        assertEquals(10, statusCounts.get(Status.REJECTED));
        assertEquals(10, statusCounts.get(Status.WAITLIST));

    }


    public Registration saveRegistration() {
        Registration eventRegistrationRequestDTO = Instancio.of(Registration.class)
                .ignore(Select.field(Registration::getId))
                .set(Select.field(Registration::getEventId), 1L)
                .create();

        return registrationRepository.save(eventRegistrationRequestDTO);
    }

    public void saveRegistrationsStatus(Status status) {
        List<Registration> registrations = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), 1L)
                        .set(Select.field(Registration::getStatus), status)
                        .create())
                .limit(10)
                .toList();
        registrationRepository.saveAll(registrations);
    }
}