package sleep.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.exceptions.SleepSessionNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.SleepSessionService;

import java.util.List;

@Service
public class SleepSessionServiceImpl implements SleepSessionService {
    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;

    @Autowired
    public SleepSessionServiceImpl(final SleepPersonRepository personRepository, final SleepSessionRepository sessionRepository) {
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public SleepSessionDto createSleepSession(SleepSessionDto sessionDto, Integer personId){
        SleepSession session = mapToObject(sessionDto);
        SleepPerson person = personRepository.findById(Long.valueOf(personId)).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gefunden werden!"));
        session.setPerson(person);
        SleepSession newSession = sessionRepository.save(session);
        return mapToDto(newSession);
    }

    @Override
    public void deleteSleepSession(Integer id){
        SleepSession session = sessionRepository.findById((long) id).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht gelöscht werden!"));
        sessionRepository.deleteById((long) id);
    }

    @Override
    public SleepSessionDto updateSleepSession(SleepSessionDto sessionDto, Integer id, Integer personId){
        SleepSession session = sessionRepository.findById((long) id).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht geupdated werden!"));
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        SleepPerson sleepPerson = personRepository.findById(Long.valueOf(personId)).orElseThrow(() -> new SleepPersonNotFoundException("Eine zutreffende Person konnte nicht gefunden werden"));
        session.setPerson(sleepPerson);
        sessionRepository.save(session);
        return sessionDto;
    }

    @Override
    public SleepSessionDto getSleepSession(Integer id){
        SleepSession session = sessionRepository.findById(Long.valueOf(id)).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht gefunden werden!"));
        return mapToDto(session);
    }

    static SleepSessionDto mapToDto(SleepSession session){
        SleepSessionDto sessionDto = new SleepSessionDto();
        sessionDto.setId(session.getId());
        sessionDto.setStartTime(session.getStartTime());
        sessionDto.setEndTime(session.getEndTime());
        sessionDto.setDuration(session.getDuration());
        sessionDto.setCycles(session.getCycles());
        sessionDto.setPersonalEvaluation(session.getPersonalEvaluation());
        sessionDto.setPersonId(session.getPerson().getId());
        return sessionDto;
    }

    static SleepSession mapToObject(SleepSessionDto sessionDto){
        SleepSession session = new SleepSession();
        session.setId(sessionDto.getId());
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        return session;
    }

}
