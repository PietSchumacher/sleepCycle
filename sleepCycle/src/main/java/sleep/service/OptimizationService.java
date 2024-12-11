package sleep.service;

import sleep.models.SleepPerson;
import sleep.service.impl.OptimizationServiceImpl.OptimizationResponse;

public interface OptimizationService {
    OptimizationResponse getOptimalDurationForOneCycle(SleepPerson person);
}
