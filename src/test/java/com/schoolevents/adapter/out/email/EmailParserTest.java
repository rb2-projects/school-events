package com.schoolevents.adapter.out.email;

import com.schoolevents.domain.model.EmailMessage;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailParserTest {

    @Test
    void shouldParseSimpleTextEmail() throws Exception {
        String rawEmail = "Message-ID: <123@example.com>\r\n" +
                "Subject: Test Subject\r\n" +
                "Date: Wed, 21 Jan 2026 12:00:00 +0000\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Hello World";

        Session session = Session.getDefaultInstance(new Properties());
        InputStream is = new ByteArrayInputStream(rawEmail.getBytes(StandardCharsets.UTF_8));
        MimeMessage msg = new MimeMessage(session, is);

        EmailParser parser = new EmailParser();
        EmailMessage result = parser.parse(msg, "<123@example.com>");

        assertEquals("Test Subject", result.subject());
        assertTrue(result.plainTextBody().contains("Hello World"));
        assertEquals(0, result.attachments().size());
    }
}
