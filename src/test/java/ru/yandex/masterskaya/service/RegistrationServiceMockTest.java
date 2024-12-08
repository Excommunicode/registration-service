package ru.yandex.masterskaya.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.masterskaya.api.EventClient;
import ru.yandex.masterskaya.api.UserClient;
import ru.yandex.masterskaya.dto.EventDto;
import ru.yandex.masterskaya.dto.EventTeamDto;
import ru.yandex.masterskaya.dto.ManagerDto;
import ru.yandex.masterskaya.dto.ManagerRole;
import ru.yandex.masterskaya.dto.RegistrationCreateRequestDto;
import ru.yandex.masterskaya.dto.RegistrationDeleteRequestDto;
import ru.yandex.masterskaya.dto.RegistrationResponseDTO;
import ru.yandex.masterskaya.dto.RegistrationStatusUpdateRequestDto;
import ru.yandex.masterskaya.dto.StatusDto;
import ru.yandex.masterskaya.dto.UserResponseDTO;
import ru.yandex.masterskaya.mapper.RegistrationMapper;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.Status;
import ru.yandex.masterskaya.repository.RegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class RegistrationServiceMockTest {
    @InjectMocks
    private RegistrationServiceImpl registrationService;
    @MockBean
    private EventPublisherService eventPublisherService;
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private RegistrationMapper registrationMapper;
    @Mock
    private EventClient eventClient;
    @Mock
    private UserClient userClient;

    private static final Registration registration = Registration.builder()
            .username("Maxim")
            .email("some@email.ru")
            .phone("+432154351451354")
            .eventId(1L)
            .number(1)
            .password("ifa9p8burgher8gp")
            .status(Status.PENDING)
            .createdDateTime(LocalDateTime.now().minusDays(3))
            .build();

    private final RegistrationStatusUpdateRequestDto registrationStatusDto = RegistrationStatusUpdateRequestDto.builder()
            .status(Status.APPROVED)
            .build();

    private final EventDto eventDto = EventDto.builder()
            .id(1L)
            .name("Ice party")
            .description("P.diddy")
            .startDateTime(LocalDateTime.now().plusDays(1))
            .endDateTime(LocalDateTime.now().plusDays(8))
            .location("USA")
            .ownerId(1L)
            .build();
    private final UserResponseDTO userResponseDTO = UserResponseDTO.builder()
            .id(1L)
            .name("Vlad")
            .email("some@email.ru")
            .aboutMe("some about me")
            .build();


    @Test
    public void addRegistrationTest() {
        Registration registration = new Registration();
        Mockito.when(eventClient.getEventById(1L)).thenReturn(new EventDto());
        Mockito.when(userClient.findByEmail("some@email.ru")).thenReturn(userResponseDTO);
        Mockito.when(registrationMapper.toModel(Mockito.any(), Mockito.anyString())).thenReturn(new Registration());
        Mockito.when(registrationRepository.saveAndReturn(new Registration())).thenReturn(new Registration());
        Mockito.when(registrationMapper.toDto(new Registration())).thenReturn(new RegistrationResponseDTO());


        RegistrationCreateRequestDto requestDto = RegistrationCreateRequestDto.builder()
                .email("some@email.ru")
                .eventId(1L)
                .build();
        registrationService.addRegistration(requestDto);


        Mockito.verify(eventClient).getEventById(1L);
        Mockito.verify(registrationRepository).saveAndReturn(registration);
        Mockito.verify(registrationMapper).toDto(registration);
    }

    @Test
    public void updateRegistrationStatus() {
        EventTeamDto eventTeamDto = EventTeamDto.builder()
                .eventId(1L)
                .personnel(List.of(ManagerDto.builder()
                        .userId(1L)
                        .role(ManagerRole.MANAGER)
                        .build()))
                .build();
        Mockito.when(registrationRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(registration));
        Mockito.when(eventClient.getEventTeam(registration.getEventId())).thenReturn(eventTeamDto);
        Mockito.when(registrationRepository.updateRegistrationById(registration)).thenReturn(1);
        registrationService.updateRegistrationStatus(registrationStatusDto, 1L, 1L);

        Mockito.verify(registrationRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(eventClient, Mockito.times(1)).getEventTeam(1L);
        Mockito.verify(registrationRepository, Mockito.times(1)).updateRegistrationById(Mockito.any(Registration.class));

    }

    @Test
    public void deleteByPhoneNumberAndPasswordTest() {
        registration.setId(1L);
        Mockito.when(eventClient.getEventById(Mockito.anyLong())).thenReturn(eventDto);
        Mockito.when(registrationRepository.findByNumberAndPassword(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(Optional.of(registration));
        Mockito.doNothing().when(registrationRepository).deleteById(Mockito.anyLong());

        registrationService.deleteByPhoneNumberAndPassword(1L, RegistrationDeleteRequestDto.builder()
                .number(1)
                .password("ifa9p8burgher8gp")
                .build());

        Mockito.verify(eventClient, Mockito.times(1)).getEventById(1L);
        Mockito.verify(registrationRepository, Mockito.times(1))
                .findByNumberAndPassword(1, "ifa9p8burgher8gp");
        Mockito.verify(registrationRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    public void getStatusByEventIdAndUserIdTest() {
        Mockito.when(userClient.findById(1L)).thenReturn(userResponseDTO);
        Mockito.when(registrationRepository.findByEventIdAndEmail(1L, userResponseDTO.getEmail()))
                .thenReturn(Optional.of(new StatusDto()));

        registrationService.getStatusByEventIdAndUserId(1L, 1L);

        Mockito.verify(userClient, Mockito.times(1)).findById(1L);
        Mockito.verify(registrationRepository,
                Mockito.times(1)).findByEventIdAndEmail(1L, userResponseDTO.getEmail());
    }
}