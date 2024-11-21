package sleep.dto;

import lombok.Data;
import sleep.models.SleepPerson;

@Data
public class RegisterDto {
    private String username;
    private String password;
    private String controllPassword;
    private SleepPersonDto sleepPersonDto;
}
