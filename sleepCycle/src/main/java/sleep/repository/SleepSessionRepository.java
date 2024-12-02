package sleep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;

import java.util.Date;
import java.util.List;

public interface SleepSessionRepository extends JpaRepository<SleepSession, Long> {
    Page<SleepSession> findByPerson(SleepPerson person, Pageable pageable);
    Page<SleepSession> findByDateBetweenAndPerson(Date startDate, Date endDate, SleepPerson person, Pageable pageable);    @Query(value = "SELECT SUM(duration) FROM sleep_session WHERE person_id = :personId", nativeQuery = true)
    Long sumDurationByPersonId(@Param("personId") Long personId);
    @Query(value = "SELECT AVG(duration) FROM sleep_session WHERE person_id = :personId AND CURRENT_TIMESTAMP > end_time AND :time < start_time", nativeQuery = true)
    Double avgDurationFromTime(@Param("personId") Long personId, @Param("time") Date time);
    @Query(value = "SELECT AVG(personal_evaluation) FROM sleep_session WHERE person_id = :personId AND CURRENT_TIMESTAMP > end_time AND :time < start_time", nativeQuery = true)
    Double avgPersonalEvaluationFromTime(@Param("personId") Long personId, @Param("time") Date time);
    @Query(value = "SELECT * FROM sleep_session WHERE person_id = :personId AND CURRENT_TIMESTAMP > CURRENT_TIMESTAMP - INTERVAL '2 MONTHS' ORDER BY personal_evaluation DESC FETCH FIRST 10 ROWS ONLY", nativeQuery = true)
    List<SleepSession> sessionsOfLastTwoMonths(@Param("personId") Long personId);
    @Query(value = "SELECT * FROM sleep_session WHERE person_id = :personId ORDER BY personal_evaluation DESC FETCH FIRST 10 ROWS ONLY", nativeQuery = true)
    List<SleepSession> getAllSessions(@Param("personId") Long personId);
}
