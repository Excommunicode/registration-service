package ru.yandex.masterskaya.repository;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.masterskaya.base.TestConstant;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.enums.Status;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.model.StatusProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@RequiredArgsConstructor
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RegistrationRepositoryTest {
    @Autowired
    private RegistrationRepository registrationRepository;

    private final Registration registration = Registration.builder()
            .username(TestConstant.NAME)
            .email(TestConstant.EMAIL)
            .phone(TestConstant.PHONE)
            .eventId(2L)
            .number(2)
            .password(TestConstant.PASSWORD)
            .status(Status.PENDING)
            .createdDateTime(LocalDateTime.now())
            .build();

    @Test
    void findAllByEventId() {
        List<Registration> eventRegistrationRequestDTOS = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), TestConstant.EVENT_ID)
                        .create())
                .limit(11)
                .toList();
        registrationRepository.saveAll(eventRegistrationRequestDTOS);


        registrationRepository.save(registration);
        List<RegistrationProjection> allByEventId = registrationRepository.findAllByEventId(1L,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))).getContent();

        assertEquals(10, allByEventId.size());
        assertEquals(1L, allByEventId.getFirst().getId());
        assertEquals(10L, allByEventId.getLast().getId());
        for (RegistrationProjection registrationProjection : allByEventId) {
            assertEquals(TestConstant.EVENT_ID, registrationProjection.getEventId());
        }
    }

    @Test
    void findByNumberAndPassword() {
        List<Registration> eventRegistrationRequestDTOS = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), TestConstant.EVENT_ID)
                        .create())
                .limit(11)
                .toList();
        registrationRepository.saveAll(eventRegistrationRequestDTOS);
        registrationRepository.save(registration);


        Registration byNumberAndPassword = registrationRepository
                .findByNumberAndPassword(2, TestConstant.PASSWORD).orElse(null);

        assertNotNull(byNumberAndPassword);
        assertEquals(TestConstant.NAME, byNumberAndPassword.getUsername());
        assertEquals(TestConstant.EMAIL, byNumberAndPassword.getEmail());
        assertEquals(TestConstant.PHONE, byNumberAndPassword.getPhone());
        assertEquals(2L, byNumberAndPassword.getEventId());

    }

    @Test
    void findByStatusInAndEventId() {
        createRegistrations(3L, Status.APPROVED, 11);
        createRegistrations(3L, Status.REJECTED, 11);
        createRegistrations(3L, Status.PENDING, 11);
        createRegistrations(3L, Status.WAITLIST, 11);

        checkRegistrations(Status.APPROVED);
        checkRegistrations(Status.REJECTED);
        checkRegistrations(Status.PENDING);
        checkRegistrations(Status.WAITLIST);


    }

    @Test
    void findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc() {
        Long evenId = 2L;
        LocalDateTime beforeTenDays = LocalDateTime.now().minusDays(10);
        Registration registration1 = registration.toBuilder()
                .eventId(evenId)
                .status(Status.WAITLIST)
                .createdDateTime(beforeTenDays)
                .build();
        registrationRepository.save(registration1);

        var foundRegister = registrationRepository.findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc(evenId).orElse(null);

        assertEquals(evenId, foundRegister.getEventId());
        assertEquals(Status.WAITLIST, foundRegister.getStatus());
        assertEquals(beforeTenDays, foundRegister.getCreatedDateTime());
    }

    @Test
    void testCountByEventIdGroupByStatus_ReturnsCorrectCountsAndOrder() {
        createRegistrations(2L, Status.APPROVED, 3);
        createRegistrations(2L, Status.PENDING, 4);
        createRegistrations(2L, Status.WAITLIST, 5);
        createRegistrations(2L, Status.REJECTED, 6);

        List<StatusProjection> statusProjections = registrationRepository.countByEventIdGroupByStatus(2L);

        assertFalse(statusProjections.isEmpty());

        assertAll(
                () -> assertEquals(Status.REJECTED, statusProjections.getFirst().getStatus()),
                () -> assertEquals(Status.APPROVED, statusProjections.getLast().getStatus())
        );

        assertEquals(4, statusProjections.size());

        Map<Status, Integer> expectedCounts = Map.of(
                Status.APPROVED, 3,
                Status.PENDING, 4,
                Status.WAITLIST, 5,
                Status.REJECTED, 6
        );

        for (int i = 0; i < statusProjections.size() - 1; i++) {
            assertTrue(
                    statusProjections.get(i).getCount() >= statusProjections.get(i + 1).getCount());
        }
        for (StatusProjection projection : statusProjections) {
            assertEquals(
                    expectedCounts.get(projection.getStatus()),
                    projection.getCount()
            );
        }
    }


    @Test
    void findByEventIdAndEmail() {
        registrationRepository.save(registration);
        StatusDto statusDto = registrationRepository.findByEventIdAndEmail(2L, TestConstant.EMAIL).orElse(null);

        assertEquals(registration.getStatus(), statusDto.status());
    }

    @Test
    void findByIdDTO() {
        registrationRepository.save(registration);

        RegistrationCreateRequestDto registrationCreateRequestDto = registrationRepository.findByIdDto(1L).orElse(null);

        assertEquals(registration.getUsername(), registrationCreateRequestDto.username());
        assertEquals(registration.getEmail(), registrationCreateRequestDto.email());
        assertEquals(registration.getPhone(), registrationCreateRequestDto.phone());
        assertEquals(registration.getEventId(), registrationCreateRequestDto.eventId());
    }

    public void createRegistrations(Long eventID, Status status, int limit) {
        List<Registration> eventRegistrationRequestDTOS = Stream.generate(() -> Instancio.of(Registration.class)
                        .ignore(Select.field(Registration::getId))
                        .set(Select.field(Registration::getEventId), eventID)
                        .set(Select.field(Registration::getStatus), status)
                        .create())
                .limit(limit)
                .toList();
        registrationRepository.saveAll(eventRegistrationRequestDTOS);

    }

    public void checkRegistrations(Status status) {
        Page<Registration> rejectedRegistrations = registrationRepository.findByStatusInAndEventId(Set.of(status),
                3L, TestConstant.PAGEABLE);

        assertEquals(10, rejectedRegistrations.getSize());
        for (Registration approvedRegistration : rejectedRegistrations) {
            assertEquals(status, approvedRegistration.getStatus());
        }
    }
}