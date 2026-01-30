package com.schoolevents.adapter.out.persistence;

import com.schoolevents.domain.model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SqliteRepositoryTest {

    private File dbFile;
    private String dbUrl;
    private SqliteEventRepository eventRepository;
    private SqliteProcessedEmailRepository emailRepository;

    @BeforeEach
    void setUp() throws IOException {
        dbFile = Files.createTempFile("test-events", ".db").toFile();
        dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        SchemaInitializer.initialize(dbUrl);
        eventRepository = new SqliteEventRepository(dbUrl);
        emailRepository = new SqliteProcessedEmailRepository(dbUrl);
    }

    @AfterEach
    void tearDown() {
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void shouldSaveAndFindEvent() {
        String id = UUID.randomUUID().toString();
        Event event = new Event(id, "Test Event", LocalDateTime.now(), null, false, "Notes", 0.9, Event.Status.ACTIVE,
                false, "email1", null, null);

        eventRepository.save(event);

        Optional<Event> found = eventRepository.findByTitleAndStartDate(event.title(), event.startDate());
        assertTrue(found.isPresent());
        assertEquals(id, found.get().id());
    }

    @Test
    void shouldUpdateEventOnConflict() {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        Event event1 = new Event(id, "Test Event", now, null, false, "Notes1", 0.9, Event.Status.ACTIVE, false,
                "email1", null,
                null);
        eventRepository.save(event1);

        Event event2 = new Event(id, "Test Event Updated", now, null, false, "Notes2", 1.0, Event.Status.CANCELLED,
                false, "email2", null, null);
        eventRepository.save(event2);

        Optional<Event> found = eventRepository.findByTitleAndStartDate("Test Event", now);
        // Note: Title changed, so we might not find it by old title if we query by old
        // title implementation
        // But let's check by matching what findByTitleAndStartDate expects.
        // Actually, we updated the title, so searching by old title should fail?
        // Wait, "Test Event Updated" is the new title.

        Optional<Event> foundNewKey = eventRepository.findByTitleAndStartDate("Test Event Updated", now);
        assertTrue(foundNewKey.isPresent());
        assertEquals("Notes2", foundNewKey.get().notes());
        assertEquals(Event.Status.CANCELLED, foundNewKey.get().status());
    }

    @Test
    void shouldTrackProcessedEmails() {
        assertFalse(emailRepository.isProcessed("msg1"));

        emailRepository.markAsProcessed("msg1");

        assertTrue(emailRepository.isProcessed("msg1"));
    }
}
