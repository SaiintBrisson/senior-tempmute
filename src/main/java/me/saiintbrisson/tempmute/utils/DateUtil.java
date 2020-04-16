package me.saiintbrisson.tempmute.utils;

import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

public class DateUtil {

    public static Timestamp convertToTimestamp(String string) {
        string = string.toUpperCase();

        long time;
        try {
            time = Long.parseLong(string.substring(0, string.length() - 1));
        } catch (Exception e) {
            return null;
        }

        for(MuteTime value : MuteTime.values()) {
            if(!string.endsWith(String.valueOf(value.name().charAt(0)))) continue;

            return Timestamp.from(
                Instant.ofEpochMilli(
                    System.currentTimeMillis() + (time * value.multiplier)
                )
            );
        }

        return null;
    }

    @AllArgsConstructor
    private enum MuteTime {

        SECONDS(1000),
        MINUTES(60 * 1000),
        HOURS(60 * 60 * 1000),
        DAYS(24 * 60 * 60 * 1000),
        WEEKS(7 * 24 * 60 * 60 * 1000),
        YEARS((long) 365 * 24 * 60 * 60 * 1000);

        private long multiplier;

    }

}
