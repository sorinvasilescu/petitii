package ro.petitii.service;

import ro.petitii.model.Attachment;

public interface AttachmentService {
    Attachment save(Attachment e);
    Attachment findById(Long id);
}
