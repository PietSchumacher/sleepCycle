package sleep.service;

import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.dto.SleepSessionResponse;

import java.util.List;

public interface SleepPersonService {
    SleepPersonDto getSleepPerson(Integer id);

    SleepPersonDto createSleepPerson(SleepPersonDto session);

    SleepPersonDto updateSleepPerson(SleepPersonDto person, Integer id);

    void deleteSleepPerson(Integer personId);

    SleepSessionResponse getAllSessionsByPersonId(int id, int pageNo, int pageSize);
}
