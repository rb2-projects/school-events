package com.schoolevents.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolevents.adapter.out.persistence.SqliteEventRepository;
import com.schoolevents.domain.model.Event;
import java.io.File;
import java.util.List;

public class PlaintextCheck {
    public static void main(String[] args) throws Exception {
        SqliteEventRepository repo = new SqliteEventRepository("jdbc:sqlite:school_events.db");
        List<Event> events = repo.findAll();

        Event recurring = events.stream().filter(e -> e.isRecurring()).findFirst().orElse(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (recurring != null) {
            String json = mapper.writeValueAsString(recurring);
            System.out.println("SAMPLE RECURRING JSON:");
            System.out.println(json);
        } else {
            System.out.println("No recurring events found in DB!");
        }
    }
}
