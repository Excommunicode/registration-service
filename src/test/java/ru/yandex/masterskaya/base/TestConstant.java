package ru.yandex.masterskaya.base;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface TestConstant {
    Long EVENT_ID = 1L;
    String NAME = "Farukh";
    String EMAIL = "someemail@mail.ru";
    String PHONE = "+12345678901";
    String PASSWORD = "qwer";
    Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
}