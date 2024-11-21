package ru.yandex.masterskaya.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query(nativeQuery = true, value = """
            INSERT INTO registrations (username, email, phone, event_id, password, number)
            VALUES (:#{#registration.username},
                    :#{#registration.email},
                    :#{#registration.phone},
                    :#{#registration.eventId},
                    :#{#registration.password},
                    (SELECT COALESCE(MAX(CAST(r.number AS INTEGER)), 0) + 1
                     FROM registrations r
                     WHERE r.event_id = :eventId))
            RETURNING *
            """)
    Registration saveAndReturn(Registration registration, Long eventId);


    @Query(nativeQuery = true, value = """
            UPDATE registrations
            SET username = :username,
                email    = :email,
                phone    = :phone
            WHERE number = :number
              AND password = :password
            RETURNING *
            """)
    Optional<Registration> updateByEventIdAndNumberAndPassword(
            int number,
            String password,
            String username,
            String email,
            String phone);

    @Query("""
            SELECT new ru.yandex.masterskaya.dto.RegistrationCreateRequestDto(
                e.id,
                e.username,
                e.email,
                e.phone,
                e.eventId
            )
            FROM Registration e
            WHERE e.id = :id
            """)
    Optional<RegistrationCreateRequestDto> findByIdDTO(Long id);

    List<RegistrationProjection> findAllByEventId(Long eventId, Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE
            FROM registrations
            WHERE number = :number
            AND password = :password
            """)
    int deleteByPhoneAndPassword(int number, String password);
}
