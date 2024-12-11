package sleep.service.impl;

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

@Service
public class SleepPersonServiceImpl implements SleepPersonService {

    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;
    private UserRepository userRepository;

    public SleepPersonServiceImpl(final SleepSessionRepository sessionRepository, final SleepPersonRepository personRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SleepPersonDto createSleepPerson(SleepPersonDto personDto){
        SleepPerson person = mapToObject(personDto);
        User user = userRepository.findById(personDto.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User konnte nicht gefunden werden!"));
        person.setUser(user);
        SleepPerson newPerson = personRepository.save(person);
        user.setPerson(newPerson);
        return personDto;
    }

    @Override
    public void deleteSleepPerson(Integer id){
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht gelöscht werden!"));
        User user = person.getUser();
        userRepository.delete(user);
    }

    @Override
    public SleepPersonDto updateSleepPerson(SleepPersonDto personDto, Integer id){
        SleepPerson person = personRepository.findById((long) id).orElseThrow(() -> new SleepPersonNotFoundException("Person konnte nicht geupdated werden!"));
        person.setBirthDate(personDto.getBirthDate());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        personRepository.save(person);
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

    @Override
    public SleepSessionResponse getAllSessionsByDateAndPersonId(Date startDate, Date endDate, int personId, int pageNo, int pageSize) {
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
        SleepPerson person = new SleepPerson();
        person.setId(person.getId());
        person.setBirthDate(personDto.getBirthDate());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        return person;
    }
}
