package com.serviceforge.migrated.service;

import com.serviceforge.migrated.api.BadRequestException;

import java.time.*;
import java.time.format.DateTimeParseException;

public final class TimePolicy {
    private TimePolicy() {
    }

    public static Instant parseIsoToUtcInstant(String iso) {
        try {
            return OffsetDateTime.parse(iso).toInstant();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid datetime format; expected ISO-8601 with offset", "INVALID_DATETIME");
        }
    }

    public static Instant startOfCurrentWeekUtc() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return start.toInstant();
    }

    public static Instant endOfCurrentWeekUtcExclusive() {
        return startOfCurrentWeekUtc().plus(Duration.ofDays(7));
    }
}
