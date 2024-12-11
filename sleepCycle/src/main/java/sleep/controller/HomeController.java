package sleep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

@Controller
public class HomeController {

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

    @GetMapping("/")
    public String displayHomePage(Model model, HttpServletRequest request) {
        isLoggedIn(request, model, jwtGenerator);
        return "home";
    }

    @GetMapping("/login")
    public String displayLoginPage(HttpServletRequest request, Model model) {
        isLoggedIn(request, model, jwtGenerator);
        return "loginPage";
    }

    @GetMapping("/register")
    public String displayRegisterPage(HttpServletRequest request, Model model) {
        isLoggedIn(request, model, jwtGenerator);
        return "register";
    }

    @GetMapping("/gatherSleepSessions")
    @PreAuthorize("isAuthenticated()")
    public String displayGatherSessionsPage(Model model, HttpServletRequest request) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        String token = getJwtFromCookies(request);
        if (loggedIn) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            model.addAttribute("username", username);
            return "sessionForm";
        }
        return "loginPage";
    }


    @GetMapping("/personalOverview")
    @PreAuthorize("isAuthenticated()")
    public String displayOverview(HttpServletRequest request, Model model) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        String token = getJwtFromCookies(request);
        if (loggedIn) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            User user = userRepository.findByUsername(username).get();
            SleepPerson person = user.getPerson();
            model.addAttribute("person", person);
            model.addAttribute("username", username);
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
        else {
            return "loginPage";
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String displayProfilePage(Model model, HttpServletRequest request) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        String token = getJwtFromCookies(request);
        if (loggedIn) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            User user = userRepository.findByUsername(username).get();
            SleepPerson person = user.getPerson();
            model.addAttribute("username", username);
            model.addAttribute("person", person);
            return "profile";
        }
        return "loginPage";
    }

    @GetMapping("/optimization")
    @PreAuthorize("isAuthenticated()")
    public String displayOptimizationPage(Model model, HttpServletRequest request) {
        boolean loggedIn = isLoggedIn(request, model, jwtGenerator);
        String token = getJwtFromCookies(request);
        if (loggedIn) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            User user = userRepository.findByUsername(username).get();
            SleepPerson person = user.getPerson();
            OptimizationResponse response = optimizationService.getOptimalDurationForOneCycle(person);
            model.addAttribute("name", person.getName());
            model.addAttribute("cycleInMinutes",Math.round(response.getDuration() / 60000));
            model.addAttribute("durationMessage",response.getDurationMessage());
            model.addAttribute("infoMessage",response.getMessage());
            return "optimization";
        }
        return "loginPage";
    }


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
}
