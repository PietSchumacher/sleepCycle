package sleep.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sleep.dto.SleepSessionResponse;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;
import sleep.service.SleepPersonService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of SleepPersonService to manage SleepPerson entities and their related operations.
 *
 * Provides functionality for creating, updating, deleting, and retrieving SleepPerson entities
 * as well as fetching associated SleepSessions.
 */
@Service
public class SleepPersonServiceImpl implements SleepPersonService {

    private static final Logger logger = LoggerFactory.getLogger(SleepSessionServiceImpl.class);

    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;
    private UserRepository userRepository;

    public SleepPersonServiceImpl(final SleepSessionRepository sessionRepository, final SleepPersonRepository personRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new SleepPerson based on the provided DTO.
     * Links the SleepPerson to an existing User.
     *
     * @param personDto DTO containing data for the new SleepPerson.
     * @return The created SleepPerson as a DTO.
     */
    @Override
    public SleepPersonDto createSleepPerson(SleepPersonDto personDto){
        logger.info("Erstelle Person aus dem Dto: {}", personDto.toString());
        SleepPerson person = mapToObject(personDto);
        User user = userRepository.findById(personDto.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User konnte nicht gefunden werden!"));
        person.setUser(user);
        SleepPerson newPerson = personRepository.save(person);
        user.setPerson(newPerson);
        logger.info("User: {} wurder der Person mit der Id: {} zugeordnet", user.getUsername(), newPerson.getId());
        return personDto;
    }

    /**
     * Deletes a SleepPerson and its associated User by ID.
     *
     * @param id The ID of the SleepPerson to delete.
     */
    @Override
    public void deleteSleepPerson(Integer id){
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gelöscht werden!"));
        User user = person.getUser();
        logger.info("Die Person mit der Id: {} und der User mit dem Username: {} werden gelöscht", id, user.getUsername());
        userRepository.delete(user);
    }

    /**
     * Updates the details of an existing SleepPerson using data from the provided DTO.
     *
     * @param personDto DTO containing updated data.
     * @param id The ID of the SleepPerson to update.
     * @return The updated SleepPerson as a DTO.
     */
    @Override
    public SleepPersonDto updateSleepPerson(SleepPersonDto personDto, Integer id){
        logger.info("Die Person mit der Id: {} wird verändert mit dem Dto: {}", id, personDto.toString());
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht geupdated werden!"));
        person.setBirthDate(personDto.getBirthDate());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        personRepository.save(person);
        return personDto;
    }

    /**
     * Retrieves a SleepPerson by ID and maps it to a DTO.
     *
     * @param id The ID of the SleepPerson to retrieve.
     * @return The SleepPerson as a DTO.
     */
    @Override
    public SleepPersonDto getSleepPerson(Integer id){
        logger.info("Hole die Person mit der Id: {}", id);
        SleepPerson person = personRepository.findById(Long.valueOf(id)).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gefunden werden!"));
        SleepPersonDto personDto = mapToDto(person);
        return personDto;
    }

    /**
     * Retrieves all SleepSessions for a SleepPerson, paginated.
     *
     * @param id The ID of the SleepPerson.
     * @param pageNo The page number to retrieve.
     * @param pageSize The size of each page.
     * @return A paginated response containing SleepSessions.
     */
    @Override
    public SleepSessionResponse getAllSessionsByPersonId(int id, int pageNo, int pageSize) {
        logger.info("Hole alle Sessions von der Person mit der Id: {}", id);
        logger.debug("Die PageNo sind {} und die Pagesize: {}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        SleepPerson person = personRepository.getReferenceById((long) id);
        Page<SleepSession> sessions = sessionRepository.findByPerson(person, pageable);
        List<SleepSession> listOfSessions = sessions.getContent();
        List<SleepSessionDto> content = listOfSessions.stream()
                .map(session -> (SleepSessionServiceImpl.mapToDto(session)))
                .collect(Collectors.toList());
        SleepSessionResponse sessionResponse = new SleepSessionResponse();
        sessionResponse.setContent(content);
        sessionResponse.setPageNo(sessions.getNumber());
        sessionResponse.setPageSize(sessions.getSize());
        sessionResponse.setTotalElements(sessions.getTotalElements());
        sessionResponse.setTotalPages(sessions.getTotalPages());
        sessionResponse.setLast(sessions.isLast());

        return sessionResponse;
    }

    /**
     * Retrieves all SleepSessions for a SleepPerson within a date range, paginated.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @param personId The ID of the SleepPerson.
     * @param pageNo The page number to retrieve.
     * @param pageSize The size of each page.
     * @return A paginated response containing SleepSessions.
     */
    @Override
    public SleepSessionResponse getAllSessionsByDateAndPersonId(Date startDate, Date endDate, int personId, int pageNo, int pageSize) {
        logger.info("Hole alle Sessions von der Person mit der Id: {} von {} bis {}", personId, startDate, endDate);
        logger.debug("Die PageNo sind {} und die Pagesize: {}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("startTime").ascending());
        SleepPerson person = personRepository.getReferenceById((long) personId);
        Page<SleepSession> sessions = sessionRepository.findByDateBetweenAndPerson(startDate, endDate, person, pageable);
        List<SleepSession> listOfSessions = sessions.getContent();
        List<SleepSessionDto> content = listOfSessions.stream()
                .map(session -> (SleepSessionServiceImpl.mapToDto(session)))
                .collect(Collectors.toList());
        SleepSessionResponse sessionResponse = new SleepSessionResponse();
        sessionResponse.setContent(content);
        sessionResponse.setPageNo(sessions.getNumber());
        sessionResponse.setPageSize(sessions.getSize());
        sessionResponse.setTotalElements(sessions.getTotalElements());
        sessionResponse.setTotalPages(sessions.getTotalPages());
        sessionResponse.setLast(sessions.isLast());

        return sessionResponse;
    }

    static SleepPersonDto mapToDto(SleepPerson person){
        SleepPersonDto personDto = new SleepPersonDto();
        personDto.setId(person.getId());
        personDto.setBirthDate(person.getBirthDate());
        personDto.setWeight(person.getWeight());
        personDto.setName(person.getName());
        personDto.setEmail(person.getEmail());
        personDto.setUserId(person.getUser().getId());
        return personDto;
    }

    static SleepPerson mapToObject(SleepPersonDto personDto){
        logger.debug("Generiere Personen-Objekt aus dem Dto: " + personDto.toString());
        SleepPerson person = new SleepPerson();
        person.setId(person.getId());
        person.setBirthDate(personDto.getBirthDate());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        return person;
    }
}
