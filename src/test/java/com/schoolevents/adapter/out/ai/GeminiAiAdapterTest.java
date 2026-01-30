package com.schoolevents.adapter.out.ai;

import com.schoolevents.domain.model.EmailMessage;
import com.schoolevents.domain.model.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiAiAdapterTest {

    @Test
    void shouldSkipIfDisabled() {
        GeminiAiAdapter adapter = new GeminiAiAdapter("fake-key", false);
        EmailMessage email = new EmailMessage("1", "Subj", LocalDateTime.now(), "Body", "", Collections.emptyList(),
                "sender@example.com", "en");

        List<Event> events = adapter.extractEvents(List.of(email));

        assertTrue(events.isEmpty());
    }

    // Testing actual API calls is hard without mocking HttpClient which requires
    // refactoring the class
    // to accept HttpClient dependency. For this MVP/Script, we trust the
    // integration or we refactor.
    // Given usage of standard HttpClient.newHttpClient() inside constructor, let's
    // leave as is for now.
}
