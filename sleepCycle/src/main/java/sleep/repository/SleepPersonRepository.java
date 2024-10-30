package sleep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sleep.models.SleepPerson;

public interface SleepPersonRepository extends JpaRepository<SleepPerson, Long> {

}
