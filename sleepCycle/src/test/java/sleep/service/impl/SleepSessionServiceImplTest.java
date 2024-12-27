package sleep.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.exceptions.SleepSessionNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SleepSessionServiceImplTest {

    @Mock
    private SleepSessionRepository sessionRepository;

    @Mock
    private SleepPersonRepository personRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SleepSessionServiceImpl sleepSessionService;

    private User testUser;
    private SleepPerson testPerson;
    private SleepSessionDto testSessionDto;
    private SleepSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);

        testPerson = new SleepPerson();
        testPerson.setName("Test User");
        testPerson.setBirthDate(new Date());
        testPerson.setWeight(70);
        testPerson.setUser(testUser);

        testSessionDto = new SleepSessionDto();
        testSessionDto.setPersonId(0);
        testSessionDto.setDate(new Date());
        testSessionDto.setStartTime(new Date());
        testSessionDto.setEndTime(new Date());
        testSessionDto.setDuration(70);
        testSessionDto.setCycles(4);
        testSessionDto.setPersonalEvaluation(8);

        testSession = sleepSessionService.mapToObject(testSessionDto);
        testSession.setPerson(testPerson);
    }

    @Test
    void createSleepSessionSuccess() {
        when(personRepository.findById(0L)).thenReturn(Optional.ofNullable(testPerson));
        when(sessionRepository.save(any(SleepSession.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        testSession.setId(0);
        SleepSessionDto sessionDto = sleepSessionService.createSleepSession(testSessionDto);
        SleepSessionDto sessionDto1 = sleepSessionService.mapToDto(testSession);
        assertEquals(sessionDto1, sessionDto1);
        }

    @Test
    void createSleepSessionSleepThrowsPersonNotFoundException() {
        when(personRepository.findById(0L)).thenReturn(Optional.empty());

        assertThrows(SleepPersonNotFoundException.class, () -> sleepSessionService.createSleepSession(testSessionDto), "User konnte nicht gefunden werden!");
    }

    @Test
    void deleteSleepSessionSuccess() {
        testSession.setId(0);
        when(sessionRepository.findById(0L)).thenReturn(Optional.ofNullable(testSession));

        sleepSessionService.deleteSleepSession(testSession.getId());

        verify(sessionRepository, times(1)).deleteById((long) testSession.getId());
    }

    @Test
    void deleteSleepSessionThrowsSleepSessionNotFoundException() {
        when(sessionRepository.findById(0L)).thenReturn(Optional.empty());

        assertThrows(SleepSessionNotFoundException.class, () -> sleepSessionService.deleteSleepSession(0), "Session konnte nicht gelöscht werden!");
    }

    @Test
    void updateSleepSessionSuccess() {
        SleepSessionDto updateSessionDto = new SleepSessionDto();
        updateSessionDto.setId(0);
        updateSessionDto.setPersonId(0);
        updateSessionDto.setDate(new Date());
        updateSessionDto.setStartTime(new Date());
        updateSessionDto.setEndTime(new Date());
        updateSessionDto.setDuration(30);
        updateSessionDto.setCycles(6);
        updateSessionDto.setPersonalEvaluation(9);
        when(sessionRepository.findById(0L)).thenReturn(Optional.ofNullable(testSession));
        when(personRepository.findById(0L)).thenReturn(Optional.ofNullable(testPerson));
        when(sessionRepository.save(any(SleepSession.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        SleepSessionDto sessionDto = sleepSessionService.updateSleepSession(updateSessionDto,0);
        assertEquals(sessionDto, updateSessionDto);
        }

    @Test
    void updateSleepSessionThrowsSleepSessionNotFoundException() {
        when(sessionRepository.findById(0L)).thenReturn(Optional.empty());

        assertThrows(SleepSessionNotFoundException.class, () -> sleepSessionService.updateSleepSession(testSessionDto,0), "Session konnte nicht geupdated werden!");
    }

    @Test
    void updateSleepSessionThrowsSleepPersonNotFoundException() {
        when(sessionRepository.findById(0L)).thenReturn(Optional.ofNullable(testSession));
        when(personRepository.findById(0L)).thenReturn(Optional.empty());

        assertThrows(SleepPersonNotFoundException.class, () -> sleepSessionService.updateSleepSession(testSessionDto,0), "Eine zutreffende Person konnte nicht gefunden werden");
    }

    @Test
    void getSleepSession() {
        when(sessionRepository.findById(0L)).thenReturn(Optional.ofNullable(testSession));
        assertEquals(testSessionDto, sleepSessionService.getSleepSession(testPerson.getId()));
    }
}