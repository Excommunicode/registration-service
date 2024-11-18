package ru.yandex.masterskaya.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
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
                email = :email,
                phone = :phone
            WHERE event_id = :eventId
              AND number = :number
              AND password = :password
            RETURNING *
            """)
    Registration updateByEventIdAndNumberAndPassword(Long eventId,
                                                     int number,
                                                     String password,
                                                     String username,
                                                     String email,
                                                     String phone);

    @Query("""
            SELECT new ru.yandex.masterskaya.dto.EventRegistrationRequestDTO(
                e.id,
                e.username,
                e.email,
                e.phone,
                e.eventId
            )
            FROM Registration e
            WHERE e.id = :id
            """)
    Optional<EventRegistrationRequestDTO> findByIdDTO(Long id);

    List<RegistrationProjection> findAllByEventId(Long eventId, Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE
            FROM registrations
            WHERE phone = :phone
            AND password = :password
            """)
    int deleteByPhoneAndPassword(String phone, String password);
}
