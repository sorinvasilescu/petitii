package ro.petitii.service;

import ro.petitii.model.EmailAttachment;

public interface EmailAttachmentService {
    EmailAttachment save(EmailAttachment e);
    EmailAttachment findById(Long id);
}
