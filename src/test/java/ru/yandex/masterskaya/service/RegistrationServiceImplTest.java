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
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
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
                .phone(eventRegistrationRequestDTO.getPhone())
                .password(eventRegistrationResponseDTO.getPassword())
                .build();


        Registration registration = registrationRepository.findById(1L).orElse(null);

        entityManager.clear();
        registrationService.deleteByPhoneNumberAndPassword(someDto);

        assertTrue(registrationRepository.findById(1L).isEmpty());
    }

}