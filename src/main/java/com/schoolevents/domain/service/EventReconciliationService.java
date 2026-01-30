package com.schoolevents.domain.service;

import com.schoolevents.domain.model.Event;
import com.schoolevents.domain.port.out.EventRepositoryPort;
import java.util.Optional;
import java.util.UUID;

public class EventReconciliationService {

    private final EventRepositoryPort eventRepository;

    public EventReconciliationService(EventRepositoryPort eventRepository) {
        this.eventRepository = eventRepository;
    }

    public enum ReconciliationResult {
        CREATED, UPDATED, CANCELLED, NO_ACTION
    }

    public ReconciliationResult reconcile(Event incomingEvent) {
        // Simple matching strategy: Title and StartDate
        // Ideally we would fuzzy match title, but exact match for MVP
        Optional<Event> existingEventOpt = eventRepository.findByTitleAndStartDate(
                incomingEvent.title(),
                incomingEvent.startDate());

        if (existingEventOpt.isPresent()) {
            Event existingEvent = existingEventOpt.get();
            if (incomingEvent.status() == Event.Status.CANCELLED) {
                cancelEvent(existingEvent, incomingEvent);
                return ReconciliationResult.CANCELLED;
            } else {
                updateEvent(existingEvent, incomingEvent);
                return ReconciliationResult.UPDATED;
            }
        } else {
            if (createNewEvent(incomingEvent)) {
                return ReconciliationResult.CREATED;
            }
            return ReconciliationResult.NO_ACTION;
        }
    }

    private void cancelEvent(Event existing, Event incoming) {
        Event cancelled = new Event(
                existing.id(),
                existing.title(),
                existing.startDate(),
                existing.endDate(),
                existing.allDay(),
                existing.notes(),
                existing.confidence(),
                Event.Status.CANCELLED,
                existing.isRecurring(),
                incoming.sourceEmailId(),
                incoming.sourceEmailSubject(),
                incoming.sourceEmailReceivedAt());
        eventRepository.save(cancelled);
    }

    private void updateEvent(Event existing, Event incoming) {
        // We update fields with incoming data, assuming it's newer/better
        Event updated = new Event(
                existing.id(),
                incoming.title(),
                incoming.startDate(),
                incoming.endDate(),
                incoming.allDay(),
                incoming.notes(),
                incoming.confidence(),
                Event.Status.ACTIVE,
                incoming.isRecurring(),
                incoming.sourceEmailId(),
                incoming.sourceEmailSubject(),
                incoming.sourceEmailReceivedAt());
        eventRepository.save(updated);
    }

    private boolean createNewEvent(Event incoming) {
        if (incoming.status() == Event.Status.ACTIVE || incoming.status() == Event.Status.SCHEDULED) {
            String newId = (incoming.id() == null || incoming.id().isEmpty())
                    ? UUID.randomUUID().toString()
                    : incoming.id();

            Event newEvent = new Event(
                    newId,
                    incoming.title(),
                    incoming.startDate(),
                    incoming.endDate(),
                    incoming.allDay(),
                    incoming.notes(),
                    incoming.confidence(),
                    Event.Status.ACTIVE,
                    incoming.isRecurring(),
                    incoming.sourceEmailId(),
                    incoming.sourceEmailSubject(),
                    incoming.sourceEmailReceivedAt());
            eventRepository.save(newEvent);
            return true;
        }
        return false;
    }
}
