package sleep.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sleep.dto.LoginDto;

public class RegisterController {

    @PostMapping("/register")
    public String login(@RequestParam LoginDto loginDto) {
        return "redirect:/?error=true";
    }

}
