package com.andruy.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class TimeTracker {
    private LocalDate date;
    private DayOfWeek dayOfTheWeek;

    public void test() {
        date = LocalDate.now();
        System.out.println(date); // 2016-11-16
        dayOfTheWeek = date.getDayOfWeek(); // TUESDAY
        System.out.println(dayOfTheWeek);

        System.out.println("Day of the week: number " + dayOfTheWeek.getValue());
    }

    public double getTotalHours(long endTime, long startTime) {
        return (endTime - startTime) / 3600000.0;
    }

    public double getTotalMinutes(long endTime, long startTime) {
        return (endTime - startTime) / 60000.0;
    }

    public DayOfWeek getDayOfTheWeek() {
        date = LocalDate.now();
        dayOfTheWeek = date.getDayOfWeek();
        return dayOfTheWeek;
    }
}
