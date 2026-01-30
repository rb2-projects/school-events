package com.schoolevents.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record Event(
        String id,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean allDay,
        String notes,
        Double confidence,
        Status status,
        @JsonProperty("isRecurring") boolean isRecurring,
        String sourceEmailId,
        String sourceEmailSubject,
        LocalDateTime sourceEmailReceivedAt) {
    public enum Status {
        ACTIVE,
        CANCELLED,
        SCHEDULED,
        UPDATED
    }
}
