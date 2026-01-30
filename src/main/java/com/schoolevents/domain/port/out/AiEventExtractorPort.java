package com.schoolevents.domain.port.out;

import com.schoolevents.domain.model.EmailMessage;
import com.schoolevents.domain.model.Event;
import java.util.List;

public interface AiEventExtractorPort {
    List<Event> extractEvents(List<EmailMessage> emails);
}
