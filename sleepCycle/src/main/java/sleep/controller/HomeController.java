package sleep.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sleep.dto.LoginDto;

@Controller
public class HomeController {

    @GetMapping("/")
    public String displayHomePage() {
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
    public String displayGatherSessionsPage(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        String username = user.getUsername();
        model.addAttribute("username", username);
        return "sessionForm";
    }

    @GetMapping("/personalOverview")
    public String displayOverview() {
        return "overview";
    }


}
