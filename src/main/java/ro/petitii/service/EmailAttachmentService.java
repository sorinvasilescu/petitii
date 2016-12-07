package ro.petitii.service;

import org.springframework.stereotype.Service;
import ro.petitii.model.EmailAttachment;

public interface EmailAttachmentService {
    EmailAttachment save(EmailAttachment e);
}
