package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Attachment;
import ro.petitii.model.Petition;
import ro.petitii.model.rest.RestAttachmentResponse;

public interface AttachmentService {
    Attachment saveAndDownload(Attachment e);

    Attachment save(Attachment e);

    void delete(long attachmentId);

    Attachment findById(Long id);

    RestAttachmentResponse getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection,
                                           String sortColumn);
}
