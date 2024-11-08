package sleep.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sleep.dto.SleepSessionResponse;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.SleepPersonService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SleepPersonServiceImpl implements SleepPersonService {

    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;

    public SleepPersonServiceImpl(final SleepSessionRepository sessionRepository, final SleepPersonRepository personRepository) {
        this.sessionRepository = sessionRepository;
        this.personRepository = personRepository;
    }

    @Override
    public SleepPersonDto createSleepPerson(SleepPersonDto personDto){
        SleepPerson person = mapToObject(personDto);
        SleepPerson newPerson = personRepository.save(person);
        return personDto;
    }

    @Override
    public void deleteSleepPerson(Integer id){
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gelöscht werden!"));
        personRepository.deleteById((long) id);
    }


    @Override
    public SleepPersonDto updateSleepPerson(SleepPersonDto personDto, Integer id){
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht geupdated werden!"));
        person.setAge(personDto.getAge());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        updateSessions(person, personDto.getSessions());
        personRepository.save(person);
        System.out.println(person.getSessions().size());
        return personDto;
    }

    @Override
    public SleepPersonDto getSleepPerson(Integer id){
        SleepPerson person = personRepository.findById(Long.valueOf(id)).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gefunden werden!"));
        SleepPersonDto personDto = mapToDto(person);
        return personDto;
    }

    @Override
    public SleepSessionResponse getAllSessionsByPersonId(int id, int pageNo, int pageSize) {
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

    static SleepPersonDto mapToDto(SleepPerson person){
        SleepPersonDto personDto = new SleepPersonDto();
        personDto.setId(person.getId());
        personDto.setAge(person.getAge());
        personDto.setWeight(person.getWeight());
        personDto.setName(person.getName());
        personDto.setSessions(person.getSessions().stream().map(session -> SleepSessionServiceImpl.mapToDto(session)).collect(Collectors.toList()));
        personDto.setEmail(person.getEmail());
        return personDto;
    }

    static SleepPerson mapToObject(SleepPersonDto personDto){
        SleepPerson person = new SleepPerson();
        person.setId(person.getId());
        person.setAge(personDto.getAge());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        person.setSessions(personDto.getSessions().stream().map(session -> SleepSessionServiceImpl.mapToObject(session)).collect(Collectors.toList()));
        return person;
    }

    private void updateSessions(SleepPerson person, List<SleepSessionDto> sessionDtos) {
        List<SleepSession> currentSessions = person.getSessions();

        Set<Integer> sessionDtoIds = sessionDtos.stream()
                .map(SleepSessionDto::getId)
                .collect(Collectors.toSet());

        currentSessions.removeIf(session -> !sessionDtoIds.contains(session.getId()));

        for (SleepSessionDto sessionDto : sessionDtos) {
            SleepSession currentSession = currentSessions.stream()
                    .filter(s -> s.getId() == sessionDto.getId())
                    .findFirst()
                    .orElseGet(() -> {
                        SleepSession newSession = new SleepSession();
                        newSession.setPerson(person);
                        currentSessions.add(newSession);
                        return newSession;
                    });
            currentSession.setStartTime(sessionDto.getStartTime());
            currentSession.setEndTime(sessionDto.getEndTime());
            currentSession.setDuration(sessionDto.getDuration());
            currentSession.setCycles(sessionDto.getCycles());
            currentSession.setPersonalEvaluation(sessionDto.getPersonalEvaluation());
        }
    }
}
