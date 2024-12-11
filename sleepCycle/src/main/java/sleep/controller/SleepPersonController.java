package sleep.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepPersonDto;
import sleep.dto.SleepSessionResponse;
import sleep.models.User;
import sleep.models.SleepPerson;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;

import static sleep.controller.HomeController.getJwtFromCookies;

@RestController
@RequestMapping("/api/person/")
public class SleepPersonController {

    private SleepPersonService personService;
    private JwtGenerator jwtGenerator;
    private UserRepository userRepository;

    public SleepPersonController(final SleepPersonService personService, final JwtGenerator jwtGenerator, final UserRepository userRepository) {
        this.personService = personService;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
    }

//    @GetMapping("{id}")
//    public ResponseEntity<SleepPersonDto> getSleepPerson(@PathVariable Integer id) {
//        return ResponseEntity.ok(personService.getSleepPerson(id));
//    }

    @PostMapping("create")
    public ResponseEntity<SleepPersonDto> createSleepPerson(@RequestBody SleepPersonDto person){
        return new ResponseEntity<>(personService.createSleepPerson(person), HttpStatus.CREATED);
    }

    @PutMapping("{id}/update")
    public ResponseEntity<SleepPersonDto> updateSleepPerson(@RequestBody SleepPersonDto person, @PathVariable("id") int personId, HttpServletRequest request){
        String token = getJwtFromCookies(request);
        if (token != null) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            try {
                SleepPerson sleepPerson = userRepository.findByUsername(username)
                        .filter(user -> user.getPerson() != null && user.getPerson().getId() == personId)
                        .map(User::getPerson)
                        .orElseThrow(() -> new EntityNotFoundException("Benutzer oder zugehörige Person nicht gefunden"));
            }
            catch (EntityNotFoundException e) {
                return new ResponseEntity<>(new SleepPersonDto(), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(personService.updateSleepPerson(person, personId));
        }
        return new ResponseEntity<>(new SleepPersonDto(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("{id}/delete")
    public ResponseEntity<String> deleteSleepPerson(@PathVariable("id") int personId, HttpServletResponse response){
        personService.deleteSleepPerson(personId);
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Person wurde erfolgreich gelöscht");
    }

//    @GetMapping("{id}/getSessions")
//    public ResponseEntity<SleepSessionResponse> getAllSessions(@PathVariable Integer id,
//                                               @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
//                                               @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize){
//        return new ResponseEntity<>(personService.getAllSessionsByPersonId(id, pageNo, pageSize),HttpStatus.OK);
//    }
}
