package com.schoolevents.launcher;

import com.schoolevents.adapter.out.persistence.SqliteEventRepository;
import com.schoolevents.domain.model.Event;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseMaintainer {
    private final SqliteEventRepository repository;

    public DatabaseMaintainer(String dbUrl) {
        this.repository = new SqliteEventRepository(dbUrl);
    }

    public void insertBookBagEvents() {
        System.out.println("Inserting Book Bag (return) events into database...");
        LocalDate start = LocalDate.of(2026, 1, 29);
        LocalDate end = LocalDate.of(2026, 7, 17);

        List<LocalDate> halfTermWeeks = List.of(
                LocalDate.of(2026, 2, 16),
                LocalDate.of(2026, 5, 25));

        LocalDate current = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
        int count = 0;

        while (!current.isAfter(end)) {
            final LocalDate date = current;
            boolean isHalfTerm = halfTermWeeks.stream()
                    .anyMatch(ht -> !date.isBefore(ht) && !date.isAfter(ht.plusDays(4)));

            if (!isHalfTerm) {
                String stableId = "book-bag-" + date.toString();
                Event event = new Event(
                        stableId,
                        "Book bag (return)",
                        date.atTime(9, 0),
                        date.atTime(9, 0),
                        false,
                        "Return book bag to school.",
                        1.0,
                        Event.Status.ACTIVE,
                        true,
                        "manual-entry",
                        "Manual Entry",
                        LocalDateTime.now());
                repository.save(event);
                count++;
            }
            current = current.plusWeeks(1);
        }
        System.out.println("Successfully inserted " + count + " Book bag events.");
    }
}
