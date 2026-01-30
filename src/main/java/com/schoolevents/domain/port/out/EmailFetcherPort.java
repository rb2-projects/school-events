package com.schoolevents.domain.port.out;

import com.schoolevents.domain.model.EmailMessage;
import java.util.List;

public interface EmailFetcherPort {
    List<EmailMessage> fetchUnprocessedEmails();
}
