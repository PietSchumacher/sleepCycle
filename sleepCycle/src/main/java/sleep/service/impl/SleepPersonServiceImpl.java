package sleep.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepPerson;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.service.SleepPersonService;

@Service
public class SleepPersonServiceImpl implements SleepPersonService {

    private SleepSessionRepository sessionRepository;
    private SleepPersonRepository personRepository;

    @Autowired
    public SleepPersonServiceImpl(final SleepSessionRepository sessionRepository, final SleepPersonRepository personRepository) {
        this.sessionRepository = sessionRepository;
        this.personRepository = personRepository;
    }

    public SleepPersonDto createSleepPerson(SleepPersonDto personDto){
        SleepPerson person = new SleepPerson();
        person.setAge(personDto.getAge());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        person.setSessions(personDto.getSessions());
        SleepPerson newPerson = personRepository.save(person);
        personDto.setId(newPerson.getId());
        return personDto;
    }

    public void deleteSleepPerson(SleepPersonDto personDto){
        personRepository.deleteById((long) personDto.getId());
    }

    public SleepPersonDto updateSleepPerson(SleepPersonDto personDto){
        SleepPerson person = personRepository.getReferenceById((long) personDto.getId());
        person.setAge(personDto.getAge());
        person.setEmail(personDto.getEmail());
        person.setName(personDto.getName());
        person.setWeight(personDto.getWeight());
        person.setSessions(personDto.getSessions());
        personRepository.save(person);
        return personDto;
    }

    public SleepPersonDto getSleepPerson(Integer id){
        SleepPersonDto personDto = new SleepPersonDto();
        SleepPerson person = personRepository.getReferenceById(Long.valueOf(id));
        personDto.setAge(person.getAge());
        personDto.setId(person.getId());
        personDto.setWeight(person.getWeight());
        personDto.setName(person.getName());
        personDto.setSessions(person.getSessions());
        personDto.setEmail(person.getEmail());

        return personDto;
    }

}
