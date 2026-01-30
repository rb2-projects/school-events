package com.schoolevents.domain.port.out;

public interface ProcessedEmailRepositoryPort {
    boolean isProcessed(String emailId);

    void markAsProcessed(String emailId);
}
