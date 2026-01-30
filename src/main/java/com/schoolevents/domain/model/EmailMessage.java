package com.schoolevents.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record EmailMessage(
        String id,
        String subject,
        LocalDateTime receivedAt,
        String plainTextBody,
        String htmlBody,
        List<AttachmentMetadata> attachments,
        String sender,
        String languageHint) {
    public record AttachmentMetadata(String fileName, String mimeType, byte[] data) {
    }
}
