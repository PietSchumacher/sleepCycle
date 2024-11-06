package sleep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;

import java.util.List;

public interface SleepSessionRepository extends JpaRepository<SleepSession, Long> {
    Page<SleepSession> findByPerson(SleepPerson person, Pageable pageable);
}
