package sleep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionDto;
import sleep.dto.SleepSessionResponse;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.service.SleepPersonService;
import sleep.service.impl.SleepPersonServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/person/")
public class SleepPersonController {

    private SleepPersonService personService;

    public SleepPersonController(final SleepPersonService personService) {
        this.personService = personService;
    }

    @GetMapping("{id}")
    public ResponseEntity<SleepPersonDto> getSleepPerson(@PathVariable Integer id) {
        return ResponseEntity.ok(personService.getSleepPerson(id));
    }

    @PostMapping("create")
    public ResponseEntity<SleepPersonDto> createSleepPerson(@RequestBody SleepPersonDto person){
        return new ResponseEntity<>(personService.createSleepPerson(person), HttpStatus.CREATED);
    }

    @PutMapping("{id}/update")
    public ResponseEntity<SleepPersonDto> updateSleepPerson(@RequestBody SleepPersonDto person, @PathVariable("id") int personId){
        return ResponseEntity.ok(personService.updateSleepPerson(person, personId));
    }

    @PostMapping("{id}/delete")
    public ResponseEntity<String> deleteSleepPerson(@PathVariable("id") int personId){
        personService.deleteSleepPerson(personId);
        return ResponseEntity.ok("Person wurde erfolgreich gelöscht");
    }

    @GetMapping("{id}/getSessions")
    public ResponseEntity<SleepSessionResponse> getAllSessions(@PathVariable Integer id,
                                               @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
                                               @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize){
        return new ResponseEntity<>(personService.getAllSessionsByPersonId(id, pageNo, pageSize),HttpStatus.OK);
    }





}
