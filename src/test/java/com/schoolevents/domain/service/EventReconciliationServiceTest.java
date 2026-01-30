package com.schoolevents.domain.service;

import com.schoolevents.domain.model.Event;
import com.schoolevents.domain.port.out.EventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventReconciliationServiceTest {

    private EventRepositoryPort eventRepository;
    private EventReconciliationService service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepositoryPort.class);
        service = new EventReconciliationService(eventRepository);
    }

    @Test
    void shouldCreateNewActiveEvent() {
        Event incoming = new Event(null, "Math Fair", LocalDateTime.now(), null, false, "Fun", 0.9, Event.Status.ACTIVE,
                false, "msg1", null, null);

        when(eventRepository.findByTitleAndStartDate(any(), any())).thenReturn(Optional.empty());

        service.reconcile(incoming);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event saved = captor.getValue();
        assertNotNull(saved.id());
        assertEquals("Math Fair", saved.title());
        assertEquals(Event.Status.ACTIVE, saved.status());
    }

    @Test
    void shouldNotCreateCancelledEventIfNotFound() {
        Event incoming = new Event(null, "Math Fair", LocalDateTime.now(), null, false, "Fun", 0.9,
                Event.Status.CANCELLED, false, "msg1", null, null);

        when(eventRepository.findByTitleAndStartDate(any(), any())).thenReturn(Optional.empty());

        service.reconcile(incoming);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingEvent() {
        String existingId = "uuid-123";
        LocalDateTime start = LocalDateTime.now();
        Event existing = new Event(existingId, "Math Fair", start, null, false, "Old Notes", 0.8, Event.Status.ACTIVE,
                false, "msg1", null, null);
        Event incoming = new Event(null, "Math Fair", start, null, false, "New Notes", 0.9, Event.Status.ACTIVE,
                false, "msg2", null, null);

        when(eventRepository.findByTitleAndStartDate("Math Fair", start)).thenReturn(Optional.of(existing));

        service.reconcile(incoming);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event saved = captor.getValue();
        assertEquals(existingId, saved.id()); // Should keep existing ID
        assertEquals("New Notes", saved.notes()); // Should update notes
        assertEquals("msg2", saved.sourceEmailId());
    }

    @Test
    void shouldCancelExistingEvent() {
        String existingId = "uuid-123";
        LocalDateTime start = LocalDateTime.now();
        Event existing = new Event(existingId, "Math Fair", start, null, false, "Notes", 0.8, Event.Status.ACTIVE,
                false, "msg1", null, null);
        Event incoming = new Event(null, "Math Fair", start, null, false, "Notes", 0.9, Event.Status.CANCELLED, false,
                "msg2",
                null, null);

        when(eventRepository.findByTitleAndStartDate("Math Fair", start)).thenReturn(Optional.of(existing));

        service.reconcile(incoming);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event saved = captor.getValue();
        assertEquals(existingId, saved.id());
        assertEquals(Event.Status.CANCELLED, saved.status());
    }
}
