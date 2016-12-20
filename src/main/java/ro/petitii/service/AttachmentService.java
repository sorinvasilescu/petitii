package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Attachment;
import ro.petitii.model.Petition;
import ro.petitii.model.rest.RestAttachmentResponse;

public interface AttachmentService {
    // save and download from email BodyPart
    Attachment saveAndDownload(Attachment e);

    // save attachment to db
    Attachment save(Attachment e);

    // delete attachment
    void deleteFromDisk(Attachment attachment);

    // delete attachment from petition
    void deleteFromPetition(long attachmentId);

    // delete attachment from email
    void deleteFromEmail(long attachmentId);

    // get attachment by id
    Attachment findById(Long id);

    // get attachment table content for a petition
    RestAttachmentResponse getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}
