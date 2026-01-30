package com.schoolevents.application.usecase;

import com.schoolevents.domain.model.EmailMessage;
import com.schoolevents.domain.model.Event;
import com.schoolevents.domain.port.out.AiEventExtractorPort;
import com.schoolevents.domain.port.out.EmailFetcherPort;
import com.schoolevents.domain.port.out.ProcessedEmailRepositoryPort;
import com.schoolevents.domain.service.EventReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class ProcessInboxUseCaseTest {

    private EmailFetcherPort emailFetcher;
    private AiEventExtractorPort aiExtractor;
    private EventReconciliationService reconciliationService;
    private ProcessedEmailRepositoryPort processedEmailRepository;
    private ProcessInboxUseCase useCase;

    @BeforeEach
    void setUp() {
        emailFetcher = mock(EmailFetcherPort.class);
        aiExtractor = mock(AiEventExtractorPort.class);
        reconciliationService = mock(EventReconciliationService.class);
        processedEmailRepository = mock(ProcessedEmailRepositoryPort.class);
        useCase = new ProcessInboxUseCase(emailFetcher, aiExtractor, reconciliationService, processedEmailRepository);
    }

    @Test
    void shouldProcessEmailsAndMarkAsProcessed() {
        EmailMessage email = new EmailMessage("1", "Subj", LocalDateTime.now(), "Body", "", Collections.emptyList(),
                "sender@example.com", "en");
        Event event = new Event("id", "Title", LocalDateTime.now(), null, false, "", 0.9, Event.Status.ACTIVE, false,
                "1",
                null, null);

        when(emailFetcher.fetchUnprocessedEmails()).thenReturn(List.of(email));
        when(aiExtractor.extractEvents(anyList())).thenReturn(List.of(event));

        useCase.execute();

        InOrder inOrder = inOrder(emailFetcher, aiExtractor, reconciliationService, processedEmailRepository);
        inOrder.verify(emailFetcher).fetchUnprocessedEmails();
        inOrder.verify(aiExtractor).extractEvents(anyList());
        inOrder.verify(reconciliationService).reconcile(event);
        inOrder.verify(processedEmailRepository).markAsProcessed("1");
    }

    @Test
    void shouldHandleExceptionAndNotMarkAsProcessed() {
        EmailMessage email = new EmailMessage("1", "Subj", LocalDateTime.now(), "Body", "", Collections.emptyList(),
                "sender@example.com", "en");

        when(emailFetcher.fetchUnprocessedEmails()).thenReturn(List.of(email));
        when(aiExtractor.extractEvents(anyList())).thenThrow(new RuntimeException("AI error"));

        useCase.execute();

        verify(emailFetcher).fetchUnprocessedEmails();
        verify(aiExtractor).extractEvents(anyList());
        verify(reconciliationService, never()).reconcile(any());
        verify(processedEmailRepository, never()).markAsProcessed("1");
    }
}
