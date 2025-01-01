package ru.yandex.masterskaya.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.yandex.masterskaya.model.RegistrationDeletedEvent;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventPublisherService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishRegistrationDeletedEvent(Long eventId) {
        eventPublisher.publishEvent(new RegistrationDeletedEvent(eventId));
    }
}
