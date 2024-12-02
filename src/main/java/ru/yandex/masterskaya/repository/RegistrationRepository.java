package ru.yandex.masterskaya.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.model.Status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, RegistrationCustomRepository {

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
    void deleteByPhoneAndPassword(int number, String password);

    Optional<Registration> findByNumberAndPassword(int number, String password);

    List<Registration> findByStatusInAndEventIdOrderByCreatedDateTimeAsc(Set<Status> statuses, Long eventId);

    Optional<Registration> findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc(Long eventId, Status status);

    @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.eventId = :eventId GROUP BY r.status")
    List<Object[]> countByEventIdGroupByStatus(Long eventId);
}
