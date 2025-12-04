package tmoney.co.kr.hxz.common.util;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String today() {
        return LocalDate.now(ZoneId.systemDefault()).format(DATE_FMT);
    }

    public String thirtyDaysAgo() {
        return LocalDate.now(ZoneId.systemDefault()).minusDays(30).format(DATE_FMT);
    }
}

