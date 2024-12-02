package sleep.service.impl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.OptimizationService;

import java.util.List;

@Service
public class OptimizationServiceImpl implements OptimizationService {

    @NoArgsConstructor
    @Getter
    @Setter
    public static class OptimizationResponse {
        private Long duration;
        private String message;
        private String durationMessage;
    }

    private SleepPersonRepository personRepository;
    private SleepSessionRepository sessionRepository;

    private static final long MILLISECONDS_DIVISOR = 1000 * 60;


    public OptimizationServiceImpl(final SleepPersonRepository personRepository, final SleepSessionRepository sessionRepository) {
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public OptimizationResponse getOptimalDurationForOneCycle(SleepPerson person) {
        OptimizationResponse response = new OptimizationResponse();
        List<SleepSession> sessions = sessionRepository.sessionsOfLastTwoMonths((long) person.getId());

        if (sessions == null || sessions.size() < 5) {
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

        if (avgDuration > 21600000 && avgDuration < 33000000) {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt liegt im Normalbereich");
        }
        else if (avgDuration < 21600000) {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt unter dem Normalbereich. Du solltest versuchen mehr zu schlafen!");
        } else {
            response.setDurationMessage("Deine durchschnittliche Schlafdauer (" + avgDuration / MILLISECONDS_DIVISOR + " min) liegt über dem Normalbereich. Du solltest versuchen weniger zu schlafen!");
        }

        response.setDuration((long) Math.round(totalDuration / cyclesCount));

        if (badSessions > (sessions.size() / 2)) {
            response.setMessage("Die Berechnung basiert zum größeren Teil auf eher schlechten Sessions (Bewertung schlechter als 6)");
        } else {
            response.setMessage("Die Berechnung basiert zum größeren Teil auf eher besseren Sessions (Bewertung gleich oder besser als 6)");
        }

        return response;
    }
}
