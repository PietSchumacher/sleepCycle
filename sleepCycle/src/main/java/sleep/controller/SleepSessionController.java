package sleep.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;

@RestController("/api/")
public class SleepSessionController {

    @GetMapping("session?{id}")
    public ResponseEntity<SleepSessionDto> getSleepSession(@PathVariable Integer id) {
        // Session nach einer Id ausgeben, Repo befragen
        return ResponseEntity.ok(new SleepSession());
    }

    @PostMapping("session/create")
    public ResponseEntity<SleepSession> createSleepSession(@RequestBody SleepSession session){

        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @PutMapping("session/update")
    public ResponseEntity<SleepSession> updateSleepSession(@RequestBody SleepSession session, @PathVariable("id") int sessionId){

        return ResponseEntity.ok(session);
    }

    @PostMapping("session/delete")
    public ResponseEntity<String> deleteSleepSession(@PathVariable("id") int sessionId){

        return ResponseEntity.ok("Session wurde erfolgreich gelöscht");
    }


}
