package sleep.dto;

import lombok.Data;
import sleep.models.SleepPerson;

import java.util.Date;

@Data
public class SleepSessionDto {
    private int id;
    private Date startTime;
    private Date endTime;
    private Integer duration;
    private Date date;
    private Integer cycles;
    private Integer personalEvaluation;
    private int personId;
}
