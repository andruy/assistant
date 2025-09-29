package com.andruy.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public record EmailTask(long timeframe, Email email) {
    public String getTime() {
        Date date = new Date(timeframe);
        LocalDate localDate = date.toInstant().atZone(ZoneId.of(System.getProperty("appTimezone"))).toLocalDate();
        LocalTime localTime = date.toInstant().atZone(ZoneId.of(System.getProperty("appTimezone"))).toLocalTime();

        return localDate + " at " + localTime;
    }
}
