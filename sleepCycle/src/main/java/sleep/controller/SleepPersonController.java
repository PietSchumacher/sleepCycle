package sleep.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepPersonDto;
import sleep.models.SleepPerson;
import sleep.models.User;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;

import static sleep.controller.HomeController.getJwtFromCookies;

/**
 * Rest Controller for managing SleepPerson entities.
 *
 * Provides endpoints for updating, deleting, and managing the sessions of SleepPerson entities.
 */
@RestController
@RequestMapping("/api/person/")
public class SleepPersonController {

    private static final Logger logger = LoggerFactory.getLogger(SleepPersonController.class);

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

//    @PostMapping("create")
//    public ResponseEntity<SleepPersonDto> createSleepPerson(@RequestBody SleepPersonDto person){
//        return new ResponseEntity<>(personService.createSleepPerson(person), HttpStatus.CREATED);
//    }

    /**
     * Updates the details of a SleepPerson entity using the provided DTO.
     *
     * @param person DTO containing the updated attributes for the SleepPerson.
     * @param personId ID of the SleepPerson to be updated.
     * @param request HTTP request containing user session details.
     * @return ResponseEntity containing the updated SleepPerson DTO if successful,
     *         or an empty DTO with an appropriate HTTP status in case of failure.
     */
    @PutMapping("{id}/update")
    public ResponseEntity<SleepPersonDto> updateSleepPerson(@RequestBody SleepPersonDto person, @PathVariable("id") int personId, HttpServletRequest request){
        logger.info("Update SleepPerson mit der Id {}", personId);
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
                logger.error("Benutzer konnte nicht gefunden werden",e.getMessage());
                return new ResponseEntity<>(new SleepPersonDto(), HttpStatus.NOT_FOUND);
            }
            SleepPersonDto sleepPersonDto = personService.updateSleepPerson(person, personId);
            logger.info("Update für SleepPerson mit der Id {} erfolgreich", personId);
            return ResponseEntity.ok(sleepPersonDto);
        }
        logger.info("Update für SleepPerson mit der Id {} fehlgeschlagen, da die Person einen anderen User zugeordnet ist", personId);
        return new ResponseEntity<>(new SleepPersonDto(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Deletes a SleepPerson entity by its ID and invalidates the user's session.
     *
     * @param personId ID of the SleepPerson to be deleted.
     * @param response HTTP response used to remove the authentication cookie.
     * @return ResponseEntity with success or error message and appropriate HTTP status code.
     */
    @PostMapping("{id}/delete")
    public ResponseEntity<String> deleteSleepPerson(@PathVariable("id") int personId, HttpServletResponse response, HttpServletRequest request){
        logger.info("Löschen der SleepPerson mit der Id {}", personId);
        try {
            String token = getJwtFromCookies(request);
            if (token != null) {
                String username = jwtGenerator.getUsernameFromJWT(token);
                userRepository.findByUsername(username)
                        .filter(user -> user.getPerson() != null && user.getPerson().getId() == personId)
                        .map(User::getPerson)
                        .orElseThrow(() -> new EntityNotFoundException("Benutzer oder zugehörige Person nicht gefunden"));
                personService.deleteSleepPerson(personId);
                Cookie cookie = new Cookie("auth_token", null);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                logger.info("Die Person mit der Id: {} wurde erfolgreich gelöscht", personId);
                return ResponseEntity.ok("Person wurde erfolgreich gelöscht");
            }
            return new ResponseEntity<>("Der Benutzer konnte nicht gefunden werden",HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Die Person mit der Id: {} konnte nicht gelöscht werden", personId, e);
            return new ResponseEntity<>("Die Person konnte nicht gelöscht werden", HttpStatus.UNAUTHORIZED);
        }
    }

//    @GetMapping("{id}/getSessions")
//    public ResponseEntity<SleepSessionResponse> getAllSessions(@PathVariable Integer id,
//                                               @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
//                                               @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize){
//        return new ResponseEntity<>(personService.getAllSessionsByPersonId(id, pageNo, pageSize),HttpStatus.OK);
//    }
}
