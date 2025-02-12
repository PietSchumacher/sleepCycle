package sleep.dto;

import lombok.Data;

import java.util.List;

@Data
public class SleepSessionResponse {
    private List<SleepSessionDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
