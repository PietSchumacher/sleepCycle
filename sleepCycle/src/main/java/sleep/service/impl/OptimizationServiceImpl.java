package sleep.service.impl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sleep.controller.SleepSessionController;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.OptimizationService;

import java.util.List;

/**
 * Implementation of the OptimizationService interface.
 *
 * Provides methods to calculate optimized sleep durations based on user sleep sessions.
 * This service analyzes user data to determine average sleep cycles and duration
 * recommendations based on historical sleep data.
 */
@Service
public class OptimizationServiceImpl implements OptimizationService {

    /**
     * Inner class to represent the response of the optimization process.
     */
    @NoArgsConstructor
    @Getter
    @Setter
    public static class OptimizationResponse {
        private Long duration;
        private String message;
        private String durationMessage;
    }

    private static final Logger logger = LoggerFactory.getLogger(OptimizationServiceImpl.class);

    private SleepPersonRepository personRepository;
    private SleepSessionRepository sessionRepository;

    private static final long MILLISECONDS_DIVISOR = 1000 * 60;


    public OptimizationServiceImpl(final SleepPersonRepository personRepository, final SleepSessionRepository sessionRepository) {
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Calculates the optimal sleep duration for one cycle based on a user's sleep sessions.
     *
     * @param person The SleepPerson entity for whom the optimization is calculated.
     * @return OptimizationResponse containing optimal duration, feedback message,
     *         and details about the average sleep duration.
     */
    @Override
    public OptimizationResponse getOptimalDurationForOneCycle(SleepPerson person) {
        logger.info("Generiere die Daten für die Optimierung des Schlafes für die Person: " + person.getName());
        OptimizationResponse response = new OptimizationResponse();
        List<SleepSession> sessions = sessionRepository.sessionsOfLastTwoMonths((long) person.getId());

        if (sessions == null || sessions.size() < 5) {
            logger.info("Aufgrund der geringen Anzahl, werden alle Sessions berücksichtigt und nicht nur die aktuellen");
            sessions = sessionRepository.getAllSessions((long) person.getId());
        }

        if (sessions == null || sessions.size() < 5) {
            response.setDuration((long) -1);
            response.setMessage("Für die Berechnung wurden zu wenige Sessions erfasst!");
            response.setDurationMessage("Keine Bewertung der durchschnittlichen Schlafdauer möglich aufgrund zu weniger Sessions.");
            return response;
        }

        int badSessions = 0;
        long totalDuration = 0;
        int cyclesCount = 0;
        for (SleepSession session : sessions) {
             if (session.getPersonalEvaluation() < 6){
                 badSessions++;
             }

             if (session.getCycles() != null) {
                 cyclesCount += session.getCycles();
             } else {
                 logger.debug("Ordne der Dauer:  {} eine Zyklusanzahl zu", session.getDuration());
                 if (session.getDuration() / MILLISECONDS_DIVISOR < 395){
                     cyclesCount += 4;
                 } else if (session.getDuration() / MILLISECONDS_DIVISOR < 485) {
                     cyclesCount += 5;
                 } else {
                     cyclesCount += 6;
                 }
             }

             totalDuration += session.getDuration();
        }

        long avgDuration = totalDuration / sessions.size();

        logger.info("Generiere die Nachricht für die Bewertung der durchschnittlichen Dauer für {}",avgDuration);
        if (avgDuration > 21600000 && avgDuration < 33000000) {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt im Normalbereich");
        }
        else if (avgDuration < 21600000) {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt unter dem Normalbereich. Du solltest versuchen mehr zu schlafen!");
        } else {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt über dem Normalbereich. Du solltest versuchen weniger zu schlafen!");
        }

        response.setDuration((long) Math.round(totalDuration / cyclesCount));

        logger.info("Generiere die Nachricht über die verwendeten Sessions aufgrund von der Anzahl an {} schlechten Sessions von insgesamt {} verwendeten", badSessions, sessions.size());
        if (badSessions > (sessions.size() / 2)) {
            response.setMessage("Die Berechnung basiert zum größeren Teil auf eher schlechten Sessions (Bewertung schlechter als 6)");
        } else {
            response.setMessage("Die Berechnung basiert zum größeren Teil auf eher besseren Sessions (Bewertung gleich oder besser als 6)");
        }

        return response;
    }
}
