package ru.yandex.masterskaya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationUpdateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.service.api.RegistrationService;

import java.util.List;

@WebMvcTest(RegistrationController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RegistrationControllerTest {

    @MockBean
    private RegistrationService registrationService;

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    private static final RegistrationCreateRequestDto registration = RegistrationCreateRequestDto.builder()
            .id(1L)
            .username("Farukh")
            .email("someemail@mail.ru")
            .phone("+12345678901")
            .eventId(1L)
            .build();


    private static final RegistrationResponseDTO registerDto = RegistrationResponseDTO.builder()
            .number(1)
            .password("1234")
            .build();

    private static final RegistrationUpdateRequestDto eventRegistrationDto = RegistrationUpdateRequestDto.builder()
            .username("Farukh")
            .email("someemail@mail.ru")
            .phone("+12345678901")
            .build();


    @Test
    @SneakyThrows
    @DisplayName("Добавление регистрации в контролере")
    void addRegistration() {

        Mockito.when(registrationService.addRegistration(Mockito.any(RegistrationCreateRequestDto.class)))
                .thenReturn(registerDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(registerDto)));
    }

    @Test
    @SneakyThrows
    void updateRegistration() {
        Mockito.when(registrationService.updateRegistration(Mockito.any(RegistrationUpdateRequestDto.class)))
                .thenReturn(eventRegistrationDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRegistrationDto)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @SneakyThrows
    void getRegistration() {
        Mockito.when(registrationService.getRegistration(Mockito.anyLong())).thenReturn(registration);

        mockMvc.perform(MockMvcRequestBuilders.get("/registrations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRegistrationDto)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @SneakyThrows
    void getAllByEventId() {
        Pageable pageable = PageRequest.of(0, 20);
        List<RegistrationCreateRequestDto> eventRegistrationRequestDTOS = List.of(registration);

        Mockito.when(registrationService.getAllByEventId(1L, pageable)).thenReturn(eventRegistrationRequestDTOS);

        mockMvc.perform(MockMvcRequestBuilders.get("/registrations")
                        .param("eventId", "1")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRegistrationRequestDTOS)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @SneakyThrows
    void deleteByPhoneAndPassword() {
        int phone = 1;
        String password = "securePassword123";
        RegistrationDeleteRequestDto someDto = RegistrationDeleteRequestDto.builder()
                .number(phone)
                .password(password)
                .build();


        Mockito.doNothing().when(registrationService).deleteByPhoneNumberAndPassword(someDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(someDto)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }
}