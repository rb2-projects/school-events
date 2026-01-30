package com.schoolevents.launcher;

import com.schoolevents.adapter.out.ai.GeminiAiAdapter;
import com.schoolevents.adapter.out.cloud.GoogleDriveStorageAdapter;
import com.schoolevents.adapter.out.email.GmailImapAdapter;
import com.schoolevents.adapter.out.filesystem.JsonExporter;
import com.schoolevents.adapter.out.persistence.SchemaInitializer;
import com.schoolevents.adapter.out.persistence.SqliteEventRepository;
import com.schoolevents.adapter.out.persistence.SqliteProcessedEmailRepository;
import com.schoolevents.application.usecase.ProcessInboxUseCase;
import com.schoolevents.domain.port.out.StoragePort;
import com.schoolevents.domain.service.EventReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {
    public static void main(String[] args) {
        // Load configuration
        String gmailUsername = System.getenv("GMAIL_USERNAME");
        String gmailPassword = System.getenv("GMAIL_PASSWORD");
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        String aiEnabledStr = System.getenv("AI_ENABLED");
        boolean aiEnabled = Boolean.parseBoolean(aiEnabledStr == null ? "true" : aiEnabledStr);
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null)
            dbUrl = "jdbc:sqlite:school_events.db";

        // Cloud & Security Config
        String googleCreds = System.getenv("GOOGLE_CREDENTIALS_JSON");
        String driveFolder = System.getenv("DRIVE_FOLDER_ID");
        String uiPassword = System.getenv("UI_PASSWORD");
        if (uiPassword == null || uiPassword.isBlank()) {
            uiPassword = "dogdog21";
        }

        // Validate config
        if (gmailUsername == null || gmailPassword == null) {
            System.err.println("Error: GMAIL_USERNAME and GMAIL_PASSWORD environment variables are required.");
            System.exit(1);
        }
        if (aiEnabled && (geminiApiKey == null || geminiApiKey.isBlank())) {
            System.err.println("Error: GEMINI_API_KEY is required when AI_ENABLED is true.");
            System.exit(1);
        }

        System.out.println("Starting School Events Organizer...");

        try {
            // Initialize DB
            SchemaInitializer.initialize(dbUrl);

            // Run database maintenance (restore manual events)
            new DatabaseMaintainer(dbUrl).insertBookBagEvents();

            // Adapters
            String senderFilter = System.getenv("SENDER_FILTER");
            var eventRepo = new SqliteEventRepository(dbUrl);
            var emailRepo = new SqliteProcessedEmailRepository(dbUrl);
            var emailFetcher = new GmailImapAdapter(gmailUsername, gmailPassword, senderFilter, emailRepo);
            var aiExtractor = new GeminiAiAdapter(geminiApiKey, aiEnabled);

            // Cloud Storage Adapter
            com.schoolevents.domain.port.out.StoragePort storagePort = null;
            if (googleCreds != null && driveFolder != null) {
                System.out.println("Enabling Google Drive Upload...");
                storagePort = new com.schoolevents.adapter.out.cloud.GoogleDriveStorageAdapter(googleCreds,
                        driveFolder);
            } else {
                System.out
                        .println("Google Drive Upload Disabled (Missing GOOGLE_CREDENTIALS_JSON or DRIVE_FOLDER_ID).");
            }

            // Domain Service
            var reconciliationService = new EventReconciliationService(eventRepo);

            // Use Case
            var processInbox = new ProcessInboxUseCase(emailFetcher, aiExtractor, reconciliationService, emailRepo);

            // Execute
            processInbox.execute();

            // Log Summary of current DB state
            var allEvents = eventRepo.findAll();
            long recurringCount = allEvents.stream().filter(e -> e.isRecurring()).count();
            System.out.println("Current Database Status:");
            System.out.println("  Total Events: " + allEvents.size());
            System.out.println("  Recurring Events: " + recurringCount);

            // debug
            if (!allEvents.isEmpty()) {
                System.out.println("DEBUG: First event JSON: "
                        + new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(allEvents.get(0)));
            }

            // Export (Crypto involved inside)
            var exporter = new JsonExporter(eventRepo, "output/events.json", storagePort, uiPassword);
            exporter.export();

            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
