package sleep.service;

import sleep.dto.SleepSessionDto;
import sleep.dto.SleepSessionResponse;

import java.util.List;

public interface SleepSessionService {
    SleepSessionDto getSleepSession(Integer id);

    SleepSessionDto createSleepSession(SleepSessionDto session);

    SleepSessionDto updateSleepSession(SleepSessionDto session, Integer id);

    void deleteSleepSession(Integer sessionId);
}
