package sleep.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sleep.controller.SleepSessionController;
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

/**
 * Implementation of SleepSessionService to manage SleepSession entities.
 *
 * Provides methods to create, update, delete, and retrieve SleepSession entities.
 */
@Service
public class SleepSessionServiceImpl implements SleepSessionService {

    private static final Logger logger = LoggerFactory.getLogger(SleepSessionServiceImpl.class);

    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;

    public SleepSessionServiceImpl(final SleepPersonRepository personRepository, final SleepSessionRepository sessionRepository) {
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Creates a new SleepSession based on the provided DTO.
     * Links the session to an existing SleepPerson.
     *
     * @param sessionDto DTO containing data for the new SleepSession.
     * @return The created SleepSession as a DTO.
     */
    @Override
    public SleepSessionDto createSleepSession(SleepSessionDto sessionDto){
        logger.info("Erstelle Session aus dem Dto: {}", sessionDto.toString());
        SleepSession session = mapToObject(sessionDto);
        SleepPerson person = personRepository.findById(Long.valueOf(sessionDto.getPersonId())).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gefunden werden!"));
        session.setPerson(person);
        SleepSession newSession = sessionRepository.save(session);
        return mapToDto(newSession);
    }

    /**
     * Deletes a SleepSession by ID.
     *
     * @param id The ID of the SleepSession to delete.
     */
    @Override
    public void deleteSleepSession(Integer id){
        logger.info("Lösche Session mit der id" + id);
        SleepSession session = sessionRepository.findById((long) id).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht gelöscht werden!"));
        sessionRepository.deleteById((long) id);
    }

    /**
     * Updates an existing SleepSession using data from the provided DTO.
     *
     * @param sessionDto DTO containing updated data for the SleepSession.
     * @param id The ID of the SleepSession to update.
     * @return The updated SleepSession as a DTO.
     */
    @Override
    public SleepSessionDto updateSleepSession(SleepSessionDto sessionDto, Integer id){
        logger.info("Verändere Session mit der id {} und den Werten: {}", id, sessionDto.toString());
        SleepSession session = sessionRepository.findById((long) id).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht geupdated werden!"));
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setDate(sessionDto.getDate());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        SleepPerson sleepPerson = personRepository.findById(Long.valueOf(sessionDto.getPersonId())).orElseThrow(() -> new SleepPersonNotFoundException("Eine zutreffende Person konnte nicht gefunden werden"));
        session.setPerson(sleepPerson);
        sessionRepository.save(session);
        sessionDto.setPersonId(sleepPerson.getId());
        sessionDto.setId(id);
        return sessionDto;
    }

    /**
     * Retrieves a SleepSession by ID and maps it to a DTO.
     *
     * @param id The ID of the SleepSession to retrieve.
     * @return The SleepSession as a DTO.
     */
    @Override
    public SleepSessionDto getSleepSession(Integer id){
        logger.info("Hole Session mit der Id" + id);
        SleepSession session = sessionRepository.findById(Long.valueOf(id)).orElseThrow(() -> new SleepSessionNotFoundException("Session konnte nicht gefunden werden!"));
        return mapToDto(session);
    }

    static SleepSessionDto mapToDto(SleepSession session){
        SleepSessionDto sessionDto = new SleepSessionDto();
        sessionDto.setId(session.getId());
        sessionDto.setStartTime(session.getStartTime());
        sessionDto.setEndTime(session.getEndTime());
        sessionDto.setDuration(session.getDuration());
        sessionDto.setDate(session.getDate());
        sessionDto.setCycles(session.getCycles());
        sessionDto.setPersonalEvaluation(session.getPersonalEvaluation());
        sessionDto.setPersonId(session.getPerson().getId());
        return sessionDto;
    }

    static SleepSession mapToObject(SleepSessionDto sessionDto){
        logger.debug("Generiere Session-Objekt aus dem Dto: " + sessionDto.toString());
        SleepSession session = new SleepSession();
        session.setId(sessionDto.getId());
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setDuration(sessionDto.getDuration());
        session.setDate(sessionDto.getDate());
        session.setCycles(sessionDto.getCycles());
        session.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        return session;
    }
}
