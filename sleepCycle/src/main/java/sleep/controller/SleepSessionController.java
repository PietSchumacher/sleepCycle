package sleep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.service.SleepSessionService;

@RestController("/api")
public class SleepSessionController {

    private SleepSessionService sessionService;

    @Autowired
    public SleepSessionController(final SleepSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("session?{id}")
    public ResponseEntity<SleepSessionDto> getSleepSession(@PathVariable Integer id) {
        return ResponseEntity.ok(sessionService.getSleepSession(id));
    }

    @PostMapping("session/create")
    public ResponseEntity<SleepSessionDto> createSleepSession(@RequestBody SleepSessionDto session){
        return new ResponseEntity<>(sessionService.createSleepSession(session), HttpStatus.CREATED);
    }

    @PutMapping("session?{id}/update")
    public ResponseEntity<SleepSessionDto> updateSleepSession(@RequestBody SleepSessionDto session, @PathVariable("id") int sessionId){
        return ResponseEntity.ok(sessionService.updateSleepSession(session,sessionId));
    }

    @PostMapping("session/delete")
    public ResponseEntity<String> deleteSleepSession(@PathVariable("id") int sessionId){
        sessionService.deleteSleepSession(sessionId);
        return ResponseEntity.ok("Session wurde erfolgreich gelöscht");
    }
}
