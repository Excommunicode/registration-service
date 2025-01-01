package ru.yandex.masterskaya.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.enums.Status;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationProjection;
import ru.yandex.masterskaya.model.StatusProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, RegistrationCustomRepository {

    Page<RegistrationProjection> findAllByEventId(Long eventId, Pageable pageable);

    @Query("""
            SELECT r
            FROM Registration r
            WHERE r.number = :number
            AND r.password = :password
            """)
    Optional<Registration> findByNumberAndPassword(int number, String password);

    @Query("""
            SELECT r
            FROM Registration r
            WHERE r.eventId = :eventId
            AND r.status in :statuses
            """)
    Page<Registration> findByStatusInAndEventId(Set<Status> statuses, Long eventId, Pageable pageable);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM registrations AS r
            WHERE r.event_id = :eventId
            AND r.status = 'WAITLIST'
            ORDER BY r.created_date_time
            LIMIT 1
            """)
    Optional<Registration> findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc(Long eventId);

    @Query(nativeQuery = true, value = """
            SELECT r.status AS status, COUNT(r) AS count
            FROM registrations AS r
            WHERE r.event_id = :eventId
            GROUP BY r.status
            ORDER BY count DESC
            """)
    List<StatusProjection> countByEventIdGroupByStatus(Long eventId);

    @Query("""
            SELECT new ru.yandex.masterskaya.dto.StatusDto(r.status)
            FROM Registration r
            WHERE r.eventId = :eventId
            AND r.email = :email
            """)
    Optional<StatusDto> findByEventIdAndEmail(Long eventId, String email);

    @Query("""
            SELECT new ru.yandex.masterskaya.dto.RegistrationCreateRequestDto(e.id, e.username, e.email, e.phone, e.eventId)
            FROM Registration e
            WHERE e.id = :id
            """)
    Optional<RegistrationCreateRequestDto> findByIdDto(Long id);
}
