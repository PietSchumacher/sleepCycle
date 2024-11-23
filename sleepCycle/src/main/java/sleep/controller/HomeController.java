package sleep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import sleep.models.User;
import sleep.security.JWTAuthenticationFilter;
import sleep.security.JwtGenerator;

@Controller
public class HomeController {

    private JwtGenerator jwtGenerator;
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    public HomeController(final JwtGenerator jwtGenerator, final JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtGenerator = jwtGenerator;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }



    @GetMapping("/")
    public String displayHomePage(Model model, HttpServletRequest request) {
        String token = getJwtFromCookies(request);
        if (token != null) {
            model.addAttribute("login", true);
        } else {
            model.addAttribute("login", false);
        }
        return "home";
    }

    @GetMapping("/login")
    public String displayLoginPage() {
        return "loginPage";
    }

    @GetMapping("/register")
    public String displayRegisterPage() {
        return "register";
    }

    @GetMapping("/gatherSleepSessions")
    public String displayGatherSessionsPage(Model model, HttpServletRequest request) {
        String token = getJwtFromCookies(request);
        if (token != null) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            model.addAttribute("username", username);
        }
        else {
            return "loginPage";
        }
        return "sessionForm";
    }

    @GetMapping("/personalOverview")
    public String displayOverview() {
        return "overview";
    }



    String getJwtFromCookies(HttpServletRequest request) {
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


}
