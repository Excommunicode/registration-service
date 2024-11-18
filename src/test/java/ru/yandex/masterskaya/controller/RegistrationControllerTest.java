package ru.yandex.masterskaya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.masterskaya.dto.EventRegistrationRequestDTO;
import ru.yandex.masterskaya.dto.EventRegistrationResponseDTO;
import ru.yandex.masterskaya.service.api.RegistrationService;

@WebMvcTest(RegistrationController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RegistrationControllerTest {

    @MockBean
    private RegistrationService registrationService;

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    private static final EventRegistrationRequestDTO registration = EventRegistrationRequestDTO.builder()
            .id(1L)
            .username("Farukh")
            .email("someemail@mail.ru")
            .phone("+12345678901")
            .eventId(1L)
            .build();


    private static final EventRegistrationResponseDTO registerDto = EventRegistrationResponseDTO.builder()
            .number(1)
            .password("1234")
            .build();


    @Test
    @SneakyThrows
    void addRegistration() {

        Mockito.when(registrationService.addRegistration(Mockito.any(EventRegistrationRequestDTO.class)))
                .thenReturn(registerDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(registerDto)));
    }

    @Test
    void updateRegistration() {

    }

    @Test
    void getRegistration() {
    }

    @Test
    void getAllByEventId() {
    }

    @Test
    void deleteByPhoneAndPassword() {
    }
}