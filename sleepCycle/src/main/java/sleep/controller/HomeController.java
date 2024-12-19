package sleep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sleep.dto.LoginDto;
import sleep.dto.SleepSessionDto;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepPersonRepository;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;
import sleep.security.JWTAuthenticationFilter;
import sleep.security.JwtGenerator;
import sleep.service.OptimizationService;
import sleep.service.SleepPersonService;
import sleep.service.impl.OptimizationServiceImpl.OptimizationResponse;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Controller for displaying the appropriate templates to the right URLs.
 *
 */
@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final UserRepository userRepository;
    private JwtGenerator jwtGenerator;
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    private SleepSessionRepository sleepSessionRepository;
    private SleepPersonService personService;
    private OptimizationService optimizationService;

    public HomeController(final JwtGenerator jwtGenerator, final JWTAuthenticationFilter jwtAuthenticationFilter, final UserRepository userRepository, final SleepSessionRepository sleepSessionRepository, final SleepPersonService personService, final OptimizationService optimizationService) {
        this.jwtGenerator = jwtGenerator;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userRepository = userRepository;
        this.sleepSessionRepository = sleepSessionRepository;
        this.personService = personService;
        this.optimizationService = optimizationService;
    }

    /**
     * Displays the home page.
     * @param model Model object to pass data to the view.
     * @param request HTTP request containing user session details.
     * @return Name of the home page template.
     */
    @GetMapping("/")
    public String displayHomePage(Model model, HttpServletRequest request) {
        logger.info("Gebe das Homepage-Template zurück");
        isLoggedIn(request, model, jwtGenerator);
        return "home";
    }

    /**
     * Displays the login page.
     * @param request HTTP request containing user session details.
     * @param model Model object to pass data to the view.
     * @return Name of the login page template.
     */
    @GetMapping("/login")
    public String displayLoginPage(HttpServletRequest request, Model model) {
        logger.info("Gebe das Login-Template zurück");
        isLoggedIn(request, model, jwtGenerator);
        return "loginPage";
    }

    /**
     * Displays the registration page.
     * @param request HTTP request containing user session details.
     * @param model Model object to pass data to the view.
     * @return Name of the registration page template.
     */
    @GetMapping("/register")
    public String displayRegisterPage(HttpServletRequest request, Model model) {
        logger.info("Gebe das Registrierungs-Template zurück");
        isLoggedIn(request, model, jwtGenerator);
        return "register";
    }

    /**
     * Displays the page for gathering sleep session information.
     * @param model Model object to pass data to the view.
     * @param request HTTP request containing user session details.
     * @return Template for session input or login page if not authenticated.
     */
    @GetMapping("/gatherSleepSessions")
    @PreAuthorize("isAuthenticated()")
    public String displayGatherSessionsPage(Model model, HttpServletRequest request) {
        logger.info("Versuche das Erfassungs-Template für Sleep Sessions zurück zu geben");
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        if (loggedIn) {
            User user = getAuthUser(request, jwtGenerator, userRepository);
            model.addAttribute("username", user.getUsername());
            logger.info("Authentifizierung erfolgreich. Gebe das Erfassungs-Template zurück");
            return "sessionForm";
        }
        logger.info("User noch nicht angemeldet, leite ihn auf die Loginseite");
        return "loginPage";
    }

    /**
     * Displays the user's personal sleep session overview.
     * @param request HTTP request containing user session details.
     * @param model Model object to pass data to the view.
     * @return Template for the personal overview or login page if not authenticated.
     */
    @GetMapping("/personalOverview")
    @PreAuthorize("isAuthenticated()")
    public String displayOverview(HttpServletRequest request, Model model) {
        logger.info("Versuche das Übersichts-Template für Sleep Sessions zurück zu geben");
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        if (loggedIn) {
            logger.info("User ist angemeldet, ermittle Daten für das Übersichts-Template ...");
            User user = getAuthUser(request, jwtGenerator, userRepository);
            SleepPerson person = user.getPerson();
            model.addAttribute("person", person);
            model.addAttribute("username", user.getUsername());
            // set totalSessions and totalSleepHours
            Pageable pageable;
            List<SleepSessionDto> sessions = personService.getAllSessionsByPersonId(person.getId(), 0, 10).getContent();
            model.addAttribute("totalSessions", sessions != null ? sessions.size() : 0);
            Long sumDuration = sleepSessionRepository.sumDurationByPersonId((long) person.getId());
            Long totalSleepHours = (sumDuration == null ? 0 : sumDuration) / (1000 * 60 * 60);
            model.addAttribute("totalSleepHours", totalSleepHours);
            // set averages
            LocalDateTime now = LocalDateTime.now();
            Date oneWeekAgo = Date.from(now.minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant());
            Date oneMonthAgo = Date.from(now.minusMonths(1).atZone(ZoneId.systemDefault()).toInstant());
            Date oneYearAgo = Date.from(now.minusYears(1).atZone(ZoneId.systemDefault()).toInstant());

            Double avg7DaysDuration = sleepSessionRepository.avgDurationFromTime((long) person.getId(), oneWeekAgo);
            model.addAttribute("avg7DaysDuration", avg7DaysDuration != null ? Math.round(avg7DaysDuration / (1000 * 60 * 60)) : 0);

            Double avgMonthDuration = sleepSessionRepository.avgDurationFromTime((long) person.getId(), oneMonthAgo);
            model.addAttribute("avgMonthDuration", avgMonthDuration != null ? Math.round(avgMonthDuration / (1000 * 60 * 60)) : 0);

            Double avgYearDuration = sleepSessionRepository.avgDurationFromTime((long) person.getId(), oneYearAgo);
            model.addAttribute("avgYearDuration", avgYearDuration != null ? Math.round(avgYearDuration / (1000 * 60 * 60)) : 0);

            Double avg7DaysEvaluation = sleepSessionRepository.avgPersonalEvaluationFromTime((long) person.getId(), oneWeekAgo);
            model.addAttribute("avg7DaysEvaluation", avg7DaysEvaluation != null ? Math.round(avg7DaysEvaluation) : 0);

            Double avgMonthEvaluation = sleepSessionRepository.avgPersonalEvaluationFromTime((long) person.getId(), oneMonthAgo);
            model.addAttribute("avgMonthEvaluation", avgMonthEvaluation != null ? Math.round(avgMonthEvaluation) : 0);

            Double avgYearEvaluation = sleepSessionRepository.avgPersonalEvaluationFromTime((long) person.getId(), oneYearAgo);
            model.addAttribute("avgYearEvaluation", avgYearEvaluation != null ? Math.round(avgYearEvaluation) : 0);

            return "personalOverview";
        }

        logger.info("User noch nicht angemeldet, leite ihn auf die Loginseite");
        return "loginPage";
    }

    /**
     * Displays the user's profile page.
     * @param model Model object to pass data to the view.
     * @param request HTTP request containing user session details.
     * @return Template for the profile page or login page if not authenticated.
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String displayProfilePage(Model model, HttpServletRequest request) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        if (loggedIn) {
            logger.info("User ist angemeldet, zeige das Profil");
            User user = getAuthUser(request, jwtGenerator, userRepository);
            SleepPerson person = user.getPerson();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("person", person);
            return "profile";
        }
        logger.info("User noch nicht angemeldet, leite ihn auf die Loginseite");
        return "loginPage";
    }

    /**
     * Displays the user's optimization page.
     * @param model Model object to pass data to the view.
     * @param request HTTP request containing user session details.
     * @return Template for the optimization page or login page if not authenticated.
     */
    @GetMapping("/optimization")
    @PreAuthorize("isAuthenticated()")
    public String displayOptimizationPage(Model model, HttpServletRequest request) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        if (loggedIn) {
            logger.info("User ist angemeldet, zeige die Optimierungsseite");
            User user = getAuthUser(request, jwtGenerator, userRepository);
            SleepPerson person = user.getPerson();
            OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(person);
            model.addAttribute("name", person.getName());
            model.addAttribute("cycleInMinutes",Math.round(response.getDuration() / 60000));
            model.addAttribute("durationMessage",response.getDurationMessage());
            model.addAttribute("infoMessage",response.getMessage());
            return "optimization";
        }
        logger.info("User noch nicht angemeldet, leite ihn auf die Loginseite");
        return "loginPage";
    }

    // Helper methods

    static String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    static boolean isLoggedIn(HttpServletRequest request, Model model, JwtGenerator jwtGenerator) {
        String token = getJwtFromCookies(request);
        boolean isLoggedIn = token != null;
        try {
            if (token != null)
                jwtGenerator.getUsernameFromJWT(token);
        } catch (Exception e) {
            isLoggedIn = false;
        }
        model.addAttribute("login",isLoggedIn);
        return isLoggedIn;
    }

    static User getAuthUser(HttpServletRequest request, JwtGenerator jwtGenerator, UserRepository userRepository) {
        try {
            String token = getJwtFromCookies(request);
            if (token != null) {
                String username = jwtGenerator.getUsernameFromJWT(token);
                return userRepository.findByUsername(username).orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


}
