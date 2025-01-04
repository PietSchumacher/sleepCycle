package sleep.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SleepPersonDto {
    private int id;
    private String name;
    private String email;
    private Date birthDate;
    private Integer weight;
    private List<SleepSessionDto> sessions;
    private int userId;
}
