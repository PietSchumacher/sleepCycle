package sleep.service;

import sleep.dto.SleepSessionDto;

import java.util.List;

public interface SleepSessionService {
    SleepSessionDto getSleepSession(Integer id);

    SleepSessionDto createSleepSession(SleepSessionDto session, Integer personId);

    SleepSessionDto updateSleepSession(SleepSessionDto session, Integer id, Integer personId);

    void deleteSleepSession(Integer sessionId);
}
