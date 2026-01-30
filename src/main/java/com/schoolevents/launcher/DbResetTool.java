package com.schoolevents.launcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbResetTool {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:school_events.db";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                Statement stmt = conn.createStatement()) {

            System.out.println("Clearing processed_emails table...");
            stmt.execute("DELETE FROM processed_emails");

            System.out.println("Clearing events table (to ensure clean re-extraction)...");
            stmt.execute("DELETE FROM events");

            System.out.println("Database reset successful. Ready for full re-extraction.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
