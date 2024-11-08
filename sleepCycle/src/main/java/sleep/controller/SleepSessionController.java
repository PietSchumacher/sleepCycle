package sleep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.service.SleepSessionService;

@RestController
@RequestMapping("/api/")
public class SleepSessionController {

    private SleepSessionService sessionService;

    public SleepSessionController(final SleepSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("session/{id}")
    public ResponseEntity<SleepSessionDto> getSleepSession(@PathVariable Integer id) {
        return ResponseEntity.ok(sessionService.getSleepSession(id));
    }

    @PostMapping("person/{personId}/session/create")
    public ResponseEntity<SleepSessionDto> createSleepSession(@PathVariable("personId") Integer personId, @RequestBody SleepSessionDto session){
        return new ResponseEntity<>(sessionService.createSleepSession(session, personId), HttpStatus.CREATED);
    }

    @PutMapping("person/{personId}/session/{id}/update")
    public ResponseEntity<SleepSessionDto> updateSleepSession(@PathVariable("personId") Integer personId, @RequestBody SleepSessionDto session, @PathVariable("id") int sessionId){
        return ResponseEntity.ok(sessionService.updateSleepSession(session,sessionId, personId));
    }

    @PostMapping("session/{id}/delete")
    public ResponseEntity<String> deleteSleepSession(@PathVariable("id") int sessionId){
        sessionService.deleteSleepSession(sessionId);
        return ResponseEntity.ok("Session wurde erfolgreich gelöscht");
    }
}
