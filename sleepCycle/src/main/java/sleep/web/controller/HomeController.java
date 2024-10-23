package sleep.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String displayGatherSessionsPage() {
        return "sessionForm";
    }

    @GetMapping("/personalOverview")
    public String displayOverview() {
        return "overview";
    }



    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        if ("user".equals(username) && "password".equals(password)) {
            return "redirect:/home";
        }
        return "redirect:/?error=true";
    }

}
