package com.schoolevents.adapter.out.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {
    public static void initialize(String dbUrl) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                    "id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT, " +
                    "all_day INTEGER, " +
                    "notes TEXT, " +
                    "confidence REAL, " +
                    "status TEXT, " +
                    "source_email_id TEXT, " +
                    "source_email_subject TEXT, " +
                    "source_email_received_at TEXT, " +
                    "is_recurring INTEGER DEFAULT 0)");

            // Migration: Add missing columns if they don't exist
            try {
                stmt.execute("ALTER TABLE events ADD COLUMN source_email_subject TEXT");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            try {
                stmt.execute("ALTER TABLE events ADD COLUMN source_email_received_at TEXT");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            try {
                stmt.execute("ALTER TABLE events ADD COLUMN is_recurring INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Ignore if column already exists
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS processed_emails (" +
                    "email_id TEXT PRIMARY KEY, " +
                    "processed_at TEXT NOT NULL)");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
