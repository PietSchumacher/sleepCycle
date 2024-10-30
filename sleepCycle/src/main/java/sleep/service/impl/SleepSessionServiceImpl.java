package sleep.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.SleepSessionService;

@Service
public class SleepSessionServiceImpl implements SleepSessionService {
    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;

    @Autowired
    public SleepSessionServiceImpl(final SleepPersonRepository personRepository, final SleepSessionRepository sessionRepository) {
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    public SleepSessionDto createSleepSession(SleepSessionDto sessionDto){
        SleepSession session = new SleepSession();
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        session.setPerson(sessionDto.getPerson());
        SleepSession newSession = sessionRepository.save(session);
        sessionDto.setId(newSession.getId());
        return sessionDto;
    }

    public void deleteSleepSession(SleepSessionDto sessionDto){
        sessionRepository.deleteById((long) sessionDto.getId());
    }

    public SleepSessionDto updateSleepSession(SleepSessionDto sessionDto){
        SleepSession session = sessionRepository.getReferenceById((long) sessionDto.getId());
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        session.setPerson(sessionDto.getPerson());
        sessionRepository.save(session);
        return sessionDto;
    }
}
