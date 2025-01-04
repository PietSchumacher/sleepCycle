package sleep.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionResponse;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SleepPersonServiceImplTest {

    @Mock
    private SleepSessionRepository sessionRepository;

    @Mock
    private SleepPersonRepository personRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SleepPersonServiceImpl sleepPersonService;

    private User testUser;

    private SleepPersonDto testPersonDto;

    private SleepPerson testPerson;

    private SleepSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);

        testPersonDto = new SleepPersonDto();
        testPersonDto.setName("Test User");
        testPersonDto.setBirthDate(new Date());
        testPersonDto.setWeight(70);
        testPersonDto.setUserId(1);
        testPerson = sleepPersonService.mapToObject(testPersonDto);

        testSession = new SleepSession();
        testSession.setId(1);
        testSession.setPerson(testPerson);
        testSession.setDate(new Date());
        testSession.setStartTime(new Date());
        testSession.setEndTime(new Date());
        testSession.setDuration(70);
        testSession.setCycles(4);
        testSession.setPersonalEvaluation(8);
    }

    @Test
    void createSleepPersonSuccess() {
        when(userRepository.findById(1)).thenReturn(Optional.ofNullable(testUser));
        when(personRepository.save(any(SleepPerson.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        sleepPersonService.createSleepPerson(testPersonDto);
        assertEquals(testUser.getPerson().getName(), testPersonDto.getName());
        assertEquals(testUser.getPerson().getBirthDate(), testPersonDto.getBirthDate());
        assertEquals(testUser.getPerson().getWeight(), testPersonDto.getWeight());
        assertEquals(testUser.getPerson().getUser(), testUser);
        assertEquals(testUser.getPerson().getEmail(), testPersonDto.getEmail());
        assertEquals(testPersonDto.getSessions(), testPersonDto.getSessions());
    }

    @Test
    void createSleepPersonThrowsUsernameNotFoundException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> sleepPersonService.createSleepPerson(testPersonDto), "User konnte nicht gefunden werden!");
    }

    @Test
    void deleteSleepPersonSuccess() {
        testPerson.setId(1);
        testPerson.setUser(testUser);
        when(personRepository.findById(1L)).thenReturn(Optional.ofNullable(testPerson));

        sleepPersonService.deleteSleepPerson(testPerson.getId());

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteSleepPersonThrowsSleepPersonNotFoundException() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(SleepPersonNotFoundException.class, () -> sleepPersonService.deleteSleepPerson(1), "Person konnte nicht gelöscht werden!");
    }

    @Test
    void updateSleepPerson() {
        SleepPerson updatedSleepPerson = sleepPersonService.mapToObject(testPersonDto);
        updatedSleepPerson.setName("Updated name");
        updatedSleepPerson.setBirthDate(new Date());
        updatedSleepPerson.setWeight(100);
        updatedSleepPerson.setUser(testUser);
        when(personRepository.findById(1L)).thenReturn(Optional.ofNullable(testPerson));
        when(personRepository.save(any(SleepPerson.class))).thenReturn(updatedSleepPerson);

        SleepPersonDto sleepPersonDto = sleepPersonService.updateSleepPerson(sleepPersonService.mapToDto(updatedSleepPerson),1);
        assertEquals(sleepPersonService.mapToDto(updatedSleepPerson), sleepPersonDto);
    }

    @Test
    void getSleepPerson() {
        testPerson.setUser(testUser);
        when(personRepository.findById(0L)).thenReturn(Optional.ofNullable(testPerson));
        assertEquals(testPersonDto, sleepPersonService.getSleepPerson(testPerson.getId()));
    }

    @Test
    void getAllSessionsByPersonId() {
        when(personRepository.getReferenceById(0L)).thenReturn(testPerson);
        Pageable pageable = PageRequest.of(1, 1);
        Page<SleepSession> sessions = new PageImpl<SleepSession>(new ArrayList<SleepSession>());
        when(sessionRepository.findByPerson(testPerson,pageable)).thenReturn(sessions);

        SleepSessionResponse response = sleepPersonService.getAllSessionsByPersonId(0,1,1);

        assertEquals(response.getContent(), sessions.getContent());
        assertEquals(response.getTotalElements(), sessions.getTotalElements());
        assertEquals(response.getTotalPages(), sessions.getTotalPages());
        assertEquals(response.getPageNo(), sessions.getNumber());
        assertEquals(response.getPageSize(), sessions.getSize());
    }

    @Test
    void getAllSessionsByDateAndPersonId() {
        Date startDate = new Date();
        Date endDate = new Date();
        int personId = 1;
        int pageNo = 0;
        int pageSize = 5;

        testPerson.setId(1);

        List<SleepSession> sessions = new ArrayList<>();
        sessions.add(testSession);

        Page<SleepSession> page = new PageImpl<>(sessions);

        when(personRepository.getReferenceById((long) personId)).thenReturn(testPerson);
        when(sessionRepository.findByDateBetweenAndPerson(startDate, endDate, testPerson,
                        PageRequest.of(pageNo, pageSize, Sort.by("startTime").ascending())))
                .thenReturn(page);

        SleepSessionResponse response = sleepPersonService.getAllSessionsByDateAndPersonId(startDate, endDate, personId, pageNo, pageSize);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());
    }
}