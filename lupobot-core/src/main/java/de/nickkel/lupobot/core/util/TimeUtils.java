package de.nickkel.lupobot.core.util;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String format(OffsetDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = time.toLocalDateTime();
        return dateTime.format(formatter);
    }
}
