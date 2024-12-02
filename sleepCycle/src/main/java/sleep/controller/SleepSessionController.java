package sleep.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static sleep.controller.HomeController.getJwtFromCookies;
import static sleep.controller.HomeController.isLoggedIn;

@RestController
@RequestMapping("/api/session/")
public class SleepSessionController {

    private final SleepSessionRepository sleepSessionRepository;
    private SleepSessionService sessionService;
    private JwtGenerator jwtGenerator;
    private UserRepository userRepository;
    private SleepPersonService personService;

    public SleepSessionController(final SleepSessionService sessionService, final JwtGenerator jwtGenerator, final UserRepository userRepository, SleepSessionRepository sleepSessionRepository, final SleepPersonService personService) {
        this.sessionService = sessionService;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
        this.sleepSessionRepository = sleepSessionRepository;
        this.personService = personService;
    }

    @GetMapping("{id}")
    public ResponseEntity<SleepSessionDto> getSleepSession(@PathVariable Integer id) {
        return ResponseEntity.ok(sessionService.getSleepSession(id));
    }

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
            startDate = convertToDateWithTimeZone(startDate);
            endDate = convertToDateWithTimeZone(endDate);
            SleepSessionResponse response = personService.getAllSessionsByDateAndPersonId(startDate, endDate, person.getId(), pageNo, pageSize);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new SleepSessionResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("create")
    public ResponseEntity<SleepSessionDto> createSleepSession(@RequestBody SleepSessionDto session, HttpServletRequest request, Model model){
        boolean personFound = setAttrWithAuthUser(request, session);
        if (personFound) {
            return new ResponseEntity<>(sessionService.createSleepSession(session), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(new SleepSessionDto(), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("{id}/update")
    public ResponseEntity<SleepSessionDto> updateSleepSession(@PathVariable int id, @RequestBody SleepSessionDto session, HttpServletRequest request){
        boolean personFound = setAttrWithAuthUser(request, session);
        if (personFound) {
            return ResponseEntity.ok(sessionService.updateSleepSession(session,id));
        }
        return new ResponseEntity<>(new SleepSessionDto(),HttpStatus.BAD_REQUEST);
    }

    @PostMapping("{id}/delete")
    public ResponseEntity<String> deleteSleepSession(@PathVariable("id") int id, HttpServletRequest request){
        String token = getJwtFromCookies(request);
        boolean isLoggedIn = token != null;
        try {
            if (token != null)
                jwtGenerator.getUsernameFromJWT(token);
        } catch (Exception e) {
            isLoggedIn = false;
        }

        if (isLoggedIn) {
            sessionService.deleteSleepSession(id);
            return ResponseEntity.ok("Session wurde erfolgreich gelöscht");
        }
        return new ResponseEntity<>("Session konnte nicht gelöscht werden",HttpStatus.BAD_REQUEST);

    }

    private SleepSessionDto setAttributesForSessionDto(SleepSessionDto dto){
        // if date null, set automatic date attr
        if (dto.getDate() == null) {
            dto.setDate(convertToDateWithTimeZone(dto.getStartTime()));
        } else {
            dto.setDate(convertToDateWithTimeZone(dto.getDate()));
        }
        // calculate duration
        dto.setDuration((int) Math.abs(dto.getEndTime().getTime() - dto.getStartTime().getTime()));

        return dto;
    }

    private Date convertToDateWithTimeZone(Date date){
        return date == null ? new Date() : Date.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private boolean setAttrWithAuthUser(HttpServletRequest request, SleepSessionDto session){
        setAttributesForSessionDto(session);
        String token = getJwtFromCookies(request);
        if (token != null) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            userRepository.findByUsername(username).ifPresent(user -> session.setPersonId(user.getPerson().getId()));
            return true;
        }
        return false;
    }
}
