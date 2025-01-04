package sleep.service;

import sleep.dto.SleepSessionDto;

public interface SleepSessionService {
    SleepSessionDto getSleepSession(Integer id);

    SleepSessionDto createSleepSession(SleepSessionDto session);

    SleepSessionDto updateSleepSession(SleepSessionDto session, Integer id);

    void deleteSleepSession(Integer sessionId);
}
