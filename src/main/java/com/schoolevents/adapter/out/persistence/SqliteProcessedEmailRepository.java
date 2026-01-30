package com.schoolevents.adapter.out.persistence;

import com.schoolevents.domain.port.out.ProcessedEmailRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;

public class SqliteProcessedEmailRepository implements ProcessedEmailRepositoryPort {

    private final String dbUrl;

    public SqliteProcessedEmailRepository(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    @Override
    public boolean isProcessed(String emailId) {
        String sql = "SELECT 1 FROM processed_emails WHERE email_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emailId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check processed email", e);
        }
    }

    @Override
    public void markAsProcessed(String emailId) {
        String sql = "INSERT OR IGNORE INTO processed_emails (email_id, processed_at) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emailId);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark email as processed", e);
        }
    }
}
