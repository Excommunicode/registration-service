package ru.yandex.masterskaya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterskaya.enums.Status;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.RegistrationDeletedEvent;
import ru.yandex.masterskaya.repository.RegistrationRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class RegistrationEventListener {
    private final RegistrationRepository registrationRepository;

    @EventListener
    @Transactional
    public void handleWaitListUpdate(RegistrationDeletedEvent event) {
        Long eventId = event.eventId();
        Optional<Registration> waitListCandidate = registrationRepository
                .findFirstByEventIdAndStatusOrderByCreatedDateTimeAsc(eventId);

        waitListCandidate.ifPresent(candidate -> {
            log.info("Promoting candidate from WAITLIST to PENDING: {}", candidate);
            candidate.setStatus(Status.PENDING);
            registrationRepository.updateRegistrationById(candidate);
        });

    }
}
