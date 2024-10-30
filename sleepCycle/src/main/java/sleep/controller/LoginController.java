package sleep.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sleep.dto.LoginDto;

public class LoginController {

    @PostMapping("/login")
    public String login(@RequestParam LoginDto loginDto) {
        return "redirect:/?error=true";
    }

}
