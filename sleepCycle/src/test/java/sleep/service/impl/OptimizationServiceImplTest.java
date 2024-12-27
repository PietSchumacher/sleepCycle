package sleep.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptimizationServiceImplTest {

    @Mock
    private SleepPersonRepository personRepository;

    @Mock
    private SleepSessionRepository sessionRepository;

    @InjectMocks
    private OptimizationServiceImpl optimizationService;

    private SleepPerson testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new SleepPerson();
        testPerson.setId(1);
        testPerson.setName("Test User");
    }

    @Test
    void testGetOptimalDurationForOneCycleInsufficientSessions() {
        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(new ArrayList<>());

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertEquals(-1, response.getDuration());
        assertEquals("Für die Berechnung wurden zu wenige Sessions erfasst!", response.getMessage());
        assertTrue(response.getDurationMessage().contains("zu weniger Sessions"));
    }

    @Test
    void testGetOptimalDurationForOneCycleNullSessions() {
        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(null);
        when(sessionRepository.getAllSessions(1L)).thenReturn(null);

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertEquals(-1, response.getDuration());
        assertEquals("Für die Berechnung wurden zu wenige Sessions erfasst!", response.getMessage());
        assertTrue(response.getDurationMessage().contains("zu weniger Sessions"));
    }

    @Test
    void testGetOptimalDurationForOneCycleSufficientSessions() {
        List<SleepSession> sessions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SleepSession session = new SleepSession();
            session.setDuration(30000000);
            session.setCycles(4);
            session.setPersonalEvaluation(8);
            sessions.add(session);
        }

        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(sessions);

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertTrue(response.getDuration() > 0);
        assertTrue(response.getMessage().contains("basiert zum größeren Teil auf eher besseren Sessions"));
        assertTrue(response.getDurationMessage().contains("liegt im Normalbereich"));
    }

    @Test
    void testGetOptimalDurationForOneCycleBadSessionsMajority() {
        List<SleepSession> sessions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SleepSession session = new SleepSession();
            session.setDuration(20000000);
            session.setCycles(5);
            session.setPersonalEvaluation(i < 3 ? 4 : 8);
            sessions.add(session);
        }

        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(sessions);

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertTrue(response.getDuration() > 0);
        assertTrue(response.getMessage().contains("basiert zum größeren Teil auf eher schlechten Sessions"));
        assertTrue(response.getDurationMessage().contains("liegt unter dem Normalbereich"));
    }

    @Test
    void testGetOptimalDurationForOneCycleUsesFallbackSessions() {
        List<SleepSession> fallbackSessions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SleepSession session = new SleepSession();
            session.setDuration(35000000);
            session.setCycles(5);
            session.setPersonalEvaluation(7);
            fallbackSessions.add(session);
        }

        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(null);
        when(sessionRepository.getAllSessions(1L)).thenReturn(fallbackSessions);

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertTrue(response.getDuration() > 0);
        assertTrue(response.getMessage().contains("basiert zum größeren Teil auf eher besseren Sessions"));
        assertTrue(response.getDurationMessage().contains("liegt über dem Normalbereich"));
    }

    @Test
    void testGetOptimalDurationForOneCycleUsesFallbackSessions2() {
        List<SleepSession> fallbackSessions = new ArrayList<>();
        int duration = 10000000;
        for (int i = 0; i < 5; i++) {
            SleepSession session = new SleepSession();
            duration += 5000000;
            session.setDuration(duration);
            session.setPersonalEvaluation(7);
            fallbackSessions.add(session);
        }

        when(sessionRepository.sessionsOfLastTwoMonths(1L)).thenReturn(null);
        when(sessionRepository.getAllSessions(1L)).thenReturn(fallbackSessions);

        OptimizationServiceImpl.OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(testPerson);

        assertNotNull(response);
        assertTrue(response.getDuration() > 0);
        assertTrue(response.getMessage().contains("basiert zum größeren Teil auf eher besseren Sessions"));
    }
}
