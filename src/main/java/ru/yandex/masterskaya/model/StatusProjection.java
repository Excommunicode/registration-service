package ru.yandex.masterskaya.model;

import ru.yandex.masterskaya.enums.Status;

public interface StatusProjection {

    Status getStatus();

    Integer getCount();
}