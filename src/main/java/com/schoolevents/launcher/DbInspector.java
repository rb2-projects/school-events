package com.schoolevents.launcher;

import com.schoolevents.adapter.out.persistence.SqliteEventRepository;
import com.schoolevents.domain.model.Event;
import java.util.List;

public class DbInspector {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:school_events.db";
        SqliteEventRepository repo = new SqliteEventRepository(dbUrl);
        List<Event> events = repo.findAll();

        System.out.println("--- DB CONTENT START ---");
        System.out.println("Total Events: " + events.size());
        for (Event e : events) {
            System.out.println("ID: " + e.id() + " | Recurring: " + e.isRecurring() + " | Title: " + e.title()
                    + " | Date: " + e.startDate());
        }
        System.out.println("--- DB CONTENT END ---");
    }
}
