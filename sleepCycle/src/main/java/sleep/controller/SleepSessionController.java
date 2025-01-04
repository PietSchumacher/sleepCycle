package sleep.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sleep.dto.SleepSessionDto;
import sleep.dto.SleepSessionResponse;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;
import sleep.service.SleepSessionService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static sleep.controller.HomeController.*;

/**
 * Rest Controller for managing SleepSession entities.
 *
 * Provides endpoints for updating, deleting, and selecting sessions with chosen parameter.
 *
 * All operations require the user to be authenticated, ensuring that only authorized users
 * can access or modify their own sessions.
 */
@RestController
@RequestMapping("/api/session/")
public class SleepSessionController {

    private static final Logger logger = LoggerFactory.getLogger(SleepSessionController.class);

    private SleepSessionRepository sleepSessionRepository;
    private SleepSessionService sessionService;
    private JwtGenerator jwtGenerator;
    private UserRepository userRepository;
    private SleepPersonService personService;

    public SleepSessionController(final SleepSessionService sessionService, final JwtGenerator jwtGenerator, final UserRepository userRepository, final SleepSessionRepository sleepSessionRepository, final SleepPersonService personService) {
        this.sessionService = sessionService;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
        this.sleepSessionRepository = sleepSessionRepository;
        this.personService = personService;
    }

//    @GetMapping("{id}")
//    public ResponseEntity<SleepSessionDto> getSleepSession(@PathVariable Integer id) {
//        return ResponseEntity.ok(sessionService.getSleepSession(id));
//    }

    /**
     * Selects a list of sessions in between a start and end date.
     *
     * @param request HTTP request containing user session details.
     * @param model Model object to pass data to the view.
     * @param startDate Start date to select a list of sessions.
     * @param endDate End date to select a list of sessions.
     * @param pageNo The page number to retrieve.
     * @param pageSize The number of sessions per page.
     * @return A list of sessions.
     */
    @GetMapping("getByDate")
    @PreAuthorize( "isAuthenticated()")
    public ResponseEntity<SleepSessionResponse> getSleepSessionsByDate(HttpServletRequest request, Model model,
                                                                       @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                                                                       @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
                                                                       @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
                                                                       @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize){
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        String token = getJwtFromCookies(request);
        if (loggedIn) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            User user = userRepository.findByUsername(username).get();
            SleepPerson person = user.getPerson();
            try {
                startDate = convertToDateWithTimeZone(startDate);
                endDate = convertToDateWithTimeZone(endDate);
                logger.info("Ermittle Daten für den Zeitraum {} bis {} für {}",startDate,endDate,username);
                SleepSessionResponse response = personService.getAllSessionsByDateAndPersonId(startDate, endDate, person.getId(), pageNo, pageSize);
                logger.info("Daten für {} erfolgreich selektiert", username);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                logger.error("Fehler bei der Selektierung der Daten zwischen {} und {} für {}", startDate, endDate, username);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.error("Es ist kein gültiger User eingeloggt");
            return new ResponseEntity<>(new SleepSessionResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates SleepSession entity based on a DTO.
     *
     * @param session DTO containing the attributes for the SleepSession.
     * @param request HTTP request containing user session details.
     * @param model Model object to pass data to the view.
     * @return ResponseEntity containing the created SleepSession DTO if successful,
     *         or an empty DTO with an appropriate HTTP status in case of failure.
     */
    @PostMapping("create")
    public ResponseEntity<SleepSessionDto> createSleepSession(@RequestBody SleepSessionDto session, HttpServletRequest request, Model model){
        logger.info("Versuche Session zu erstellen: {}", session);
        boolean personFound = setAttrWithAuthUser(request, session);
        if (personFound) {
            try {
                SleepSessionDto sessionDto = sessionService.createSleepSession(session);
                logger.info("Session erfolgreich erstellt");
                return new ResponseEntity<>(sessionDto, HttpStatus.CREATED);
            } catch (Exception e) {
                logger.error("Session konnte nicht erstellt werden",e.getMessage());
                return new ResponseEntity<>(new SleepSessionDto(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        logger.error("Es konnte keine passende Person für die Session gefunden werden");
        return new ResponseEntity<>(new SleepSessionDto(), HttpStatus.BAD_REQUEST);
    }

    /**
     * the details of a SleepSession entity using the provided DTO.
     *
     * @param id ID of the SleepSession to be updated.
     * @param session DTO containing the updated attributes for the SleepSession.
     * @param request HTTP request containing user session details.
     * @return ResponseEntity containing the updated SleepSession DTO if successful,
     *         or an empty DTO with an appropriate HTTP status in case of failure.
     */
    @PutMapping("{id}/update")
    public ResponseEntity<SleepSessionDto> updateSleepSession(@PathVariable int id, @RequestBody SleepSessionDto session, HttpServletRequest request){
        logger.info("Versuche Session mit der Id: {} zu updaten", id);
        boolean personFound = setAttrWithAuthUser(request, session);
        if (personFound) {
            try {
                SleepSessionDto sessionDto = sessionService.updateSleepSession(session,id);
                logger.info("Session mit der Id: {} erfolgreich geupdated", id);
                return ResponseEntity.ok(sessionDto);
            } catch (Exception e) {
                logger.error("Session konnte nicht geupdated werden",e.getMessage());
                return new ResponseEntity<>(new SleepSessionDto(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        logger.error("Es konnte keine passende Person für die Session gefunden werden");
        return new ResponseEntity<>(new SleepSessionDto(),HttpStatus.BAD_REQUEST);
    }

    /**
     * Deletes a SleepSession entity by its ID.
     *
     * @param id D of the SleepSession to be deleted.
     * @param request HTTP request containing user session details.
     * @return ResponseEntity with success or error message and appropriate HTTP status code.
     */
    @PostMapping("{id}/delete")
    public ResponseEntity<String> deleteSleepSession(@PathVariable("id") int id, HttpServletRequest request){
        logger.info("Versuche Session mit der Id: {} zu löschen", id);
        User user = getAuthUser(request, jwtGenerator, userRepository);
        SleepSession session = sleepSessionRepository.findById(Long.valueOf(id)).orElse(null);
        if (user != null && session != null && session.getPerson().getUser().getUsername().equals(user.getUsername())) {
            try {
                sessionService.deleteSleepSession(id);
                return ResponseEntity.ok("Session wurde erfolgreich gelöscht");
            } catch (Exception e) {
                logger.error("Session konnte nicht gelöscht werden",e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        logger.error("Der User hat keine Rechte die Session zu löschen");
        return new ResponseEntity<>("Session konnte nicht gelöscht werden",HttpStatus.UNAUTHORIZED);
    }

    /**
     * Sets default attributes for a SleepSession DTO, including date and duration.
     *
     * If the date is null, it sets the date to the start time's timezone-adjusted value.
     * Calculates the session duration based on the start and end times, if both are provided.
     *
     * @param dto The SleepSession DTO to be updated with default values.
     * @return The updated SleepSession DTO.
     */
    private SleepSessionDto setAttributesForSessionDto(SleepSessionDto dto){
        // if date null, set automatic date attr
        if (dto.getDate() == null) {
            dto.setDate(convertToDateWithTimeZone(dto.getStartTime()));
        }
        // calculate duration
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            dto.setDuration((int) Math.abs(dto.getEndTime().getTime() - dto.getStartTime().getTime()));
        }

        return dto;
    }

    private Date convertToDateWithTimeZone(Date date) {
        if (date == null)
            return new Date();
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private boolean setAttrWithAuthUser(HttpServletRequest request, SleepSessionDto session){
        setAttributesForSessionDto(session);
        User user = getAuthUser(request, jwtGenerator, userRepository);
        SleepSession sessionObject = sleepSessionRepository.findById(Long.valueOf(session.getId())).orElse(null);
        if (sessionObject != null & user != null && session != null && sessionObject.getPerson().getId() == user.getPerson().getId()) {
            return true;
        } else if (user != null && session != null){
            session.setPersonId(user.getPerson().getId());
            return true;
        }
        return false;
    }
}