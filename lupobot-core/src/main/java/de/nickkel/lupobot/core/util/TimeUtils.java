package de.nickkel.lupobot.core.util;


import de.nickkel.lupobot.core.command.CommandContext;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String format(OffsetDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = time.toLocalDateTime();
        return dateTime.format(formatter);
    }

    public static String format(CommandContext context, long millis) {
        return String.format("%d " + context.getServer().translate(null, "core_minutes") + ", %d "
                        + context.getServer().translate(null, "core_seconds"),
                TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
