package ru.yandex.masterskaya.repository;

import ru.yandex.masterskaya.model.Registration;

public interface RegistrationCustomRepository {

    Registration saveAndReturn(Registration registration);

    Registration updateByEventIdAndNumberAndPassword(Registration registration);

    void updateRegistrationById(Registration registration);
}