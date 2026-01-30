package com.schoolevents.adapter.out.email;

import com.schoolevents.domain.model.EmailMessage;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmailParser {

    public EmailMessage parse(Message message, String messageId) throws MessagingException, IOException {
        String subject = message.getSubject();
        Date receivedDate = message.getReceivedDate();
        LocalDateTime receivedAt = receivedDate != null
                ? LocalDateTime.ofInstant(receivedDate.toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now();

        StringBuilder plainText = new StringBuilder();
        StringBuilder htmlText = new StringBuilder();
        List<EmailMessage.AttachmentMetadata> attachments = new ArrayList<>();

        extractContent(message.getContent(), plainText, htmlText, attachments);

        // Fallback: If no plain text, strip HTML? Or keep generic.
        if (plainText.length() == 0 && htmlText.length() > 0) {
            // Simple strip logic or just leave empty
        }

        String sender = "Unknown";
        if (message.getFrom() != null && message.getFrom().length > 0) {
            sender = message.getFrom()[0].toString();
        }

        return new EmailMessage(
                messageId,
                subject,
                receivedAt,
                plainText.toString().trim(),
                htmlText.toString().trim(),
                attachments,
                sender,
                "en" // Default or detect
        );
    }

    private void extractContent(Object content, StringBuilder plainText, StringBuilder htmlText,
            List<EmailMessage.AttachmentMetadata> attachments) throws MessagingException, IOException {
        if (content instanceof String) {
            plainText.append((String) content);
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) ||
                        (part.getDisposition() != null && !Part.INLINE.equalsIgnoreCase(part.getDisposition()))) { // Some
                                                                                                                   // clients
                                                                                                                   // don't
                                                                                                                   // set
                                                                                                                   // disposition
                                                                                                                   // correctly,
                                                                                                                   // but
                                                                                                                   // check
                                                                                                                   // filename

                    if (part.getFileName() != null) {
                        String fileName = MimeUtility.decodeText(part.getFileName());
                        byte[] data = part.getInputStream().readAllBytes();
                        attachments.add(new EmailMessage.AttachmentMetadata(fileName, part.getContentType(), data));
                    }
                } else {
                    // Inline or body
                    if (part.isMimeType("text/plain")) {
                        plainText.append(part.getContent());
                    } else if (part.isMimeType("text/html")) {
                        htmlText.append(part.getContent());
                    } else if (part.getContent() instanceof Multipart) {
                        extractContent(part.getContent(), plainText, htmlText, attachments);
                    }
                }
            }
        }
    }
}
