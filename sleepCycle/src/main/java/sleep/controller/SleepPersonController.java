package sleep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sleep.dto.SleepPersonDto;
import sleep.models.SleepPerson;
import sleep.service.SleepPersonService;
import sleep.service.impl.SleepPersonServiceImpl;

@RestController("/api/")
public class SleepPersonController {

    private SleepPersonService personService;

    @Autowired
    public SleepPersonController(final SleepPersonService personService) {
        this.personService = personService;
    }

    @GetMapping("person?{id}")
    public ResponseEntity<SleepPersonDto> getSleepPerson(@PathVariable Integer id) {
        return ResponseEntity.ok(personService.getSleepPerson(id));
    }





}
