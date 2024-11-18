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
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterskaya.dto.EventRegistrationDto;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.repository.RegistrationRepository;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
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
        EventRegistrationRequestDTO eventRegistrationRequestDTO = Instancio.of(EventRegistrationRequestDTO.class)
                .ignore(Select.field(EventRegistrationRequestDTO::getId))
                .create();

        EventRegistrationResponseDTO addedRegistration = registrationService.addRegistration(eventRegistrationRequestDTO);

        assertNotNull(addedRegistration);
        assertEquals(addedRegistration.getNumber(), 1);
        assertNotNull(addedRegistration.getPassword());
    }

    @Test
    @DisplayName("Обновление регистрации")
    void updateRegistration() {
        EventRegistrationRequestDTO eventRegistrationRequestDTO = Instancio.of(EventRegistrationRequestDTO.class)
                .ignore(Select.field(EventRegistrationRequestDTO::getId))
                .set(Select.field(EventRegistrationRequestDTO::getEventId), 1L)
                .create();

        EventRegistrationResponseDTO addedRegistration = registrationService.addRegistration(eventRegistrationRequestDTO);

        EventRegistrationDto eventRegistrationDto = EventRegistrationDto.builder()
                .eventId(eventRegistrationRequestDTO.getEventId())
                .username("Leonid")
                .email("Leonid@mail.ru")
                .phone("+23433254")
                .number(addedRegistration.getNumber())
                .password(addedRegistration.getPassword())
                .build();

        EventRegistrationDto updateRegistration = registrationService.updateRegistration(eventRegistrationDto.getEventId(), eventRegistrationDto);
        assertNotNull(updateRegistration);
        assertEquals(updateRegistration.getNumber(), addedRegistration.getNumber());
        assertEquals(updateRegistration.getPassword(), addedRegistration.getPassword());
    }

    @Test
    @DisplayName("Получение регистрации па айди")
    void getRegistration() {
        EventRegistrationRequestDTO eventRegistrationRequestDTO = Instancio.of(EventRegistrationRequestDTO.class)
                .ignore(Select.field(EventRegistrationRequestDTO::getId))
                .set(Select.field(EventRegistrationRequestDTO::getEventId), 1L)
                .create();

        registrationService.addRegistration(eventRegistrationRequestDTO);

        EventRegistrationRequestDTO registration = registrationService.getRegistration(1L);


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

        List<EventRegistrationRequestDTO> allByEventId = registrationService.getAllByEventId(1L, pageable);

        assertEquals(10, allByEventId.size(), "Expected 10 registrations to be returned for event ID 1");

        for (EventRegistrationRequestDTO dto : allByEventId) {
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
        EventRegistrationRequestDTO eventRegistrationRequestDTO = Instancio.of(EventRegistrationRequestDTO.class)
                .ignore(Select.field(EventRegistrationRequestDTO::getId))
                .set(Select.field(EventRegistrationRequestDTO::getEventId), 1L)
                .create();


        registrationService.addRegistration(eventRegistrationRequestDTO);


        Registration registration = registrationRepository.findById(1L).orElse(null);

        entityManager.clear();
        registrationService.deleteByPhoneNumberAndPassword(registration.getPhone(), registration.getPassword());

        assertTrue(registrationRepository.findById(1L).isEmpty());
    }

}