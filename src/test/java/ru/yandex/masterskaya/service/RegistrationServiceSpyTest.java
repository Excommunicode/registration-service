//package ru.yandex.masterskaya.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.github.tomakehurst.wiremock.WireMockServer;
//import lombok.SneakyThrows;
//import org.junit.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import ru.yandex.masterskaya.api.EventClient;
//import ru.yandex.masterskaya.config.EventMocks;
//import ru.yandex.masterskaya.config.WireMockConfig;
//import ru.yandex.masterskaya.dto.EventDto;
//
//import java.util.Optional;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {WireMockConfig.class})
//public class RegistrationServiceSpyTest {
//    @Autowired
//    private WireMockServer wireMockServer;
//
//    @Autowired
//    private EventClient eventClient;
//
//    @BeforeEach
//    void setUp() throws JsonProcessingException {
//        Long eventId = 1L;
//        EventDto eventDto = new EventDto();
//        EventMocks.setupMockGetEventByIdResponse(wireMockServer, eventId, eventDto);
//    }
//
//    @Test
//    public void whenGetEventById_thenEventShouldBeReturned() {
//        Long eventId = 1L;
//        Optional<EventDto> event = eventClient.getEventById(eventId);
//        assertTrue(event.isPresent());
//        assertEquals(eventId, event.get().getId());
//    }
//}
//
//
