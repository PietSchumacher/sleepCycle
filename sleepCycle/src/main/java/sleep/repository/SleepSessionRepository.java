package sleep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sleep.models.SleepSession;

public interface SleepSessionRepository extends JpaRepository<SleepSession, Long> {
}
