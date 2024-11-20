package ru.yandex.masterskaya.model;

public interface RegistrationProjection {

    Long getId();

    String getUsername();

    String getEmail();

    String getPhone();

    Long getEventId();
}