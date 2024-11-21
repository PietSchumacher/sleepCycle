package sleep.dto;

import lombok.Data;
import sleep.models.SleepSession;
import sleep.models.User;

import java.util.List;

@Data
public class SleepPersonDto {
    private int id;
    private String name;
    private String email;
    private Integer age;
    private Integer weight;
    private List<SleepSessionDto> sessions;
    private int userId;
}
