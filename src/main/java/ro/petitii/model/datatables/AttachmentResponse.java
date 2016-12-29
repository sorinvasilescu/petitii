package ro.petitii.model.datatables;

import java.util.HashMap;
import java.util.Map;

public class AttachmentResponse {
    public static final Map<String, String> sortMapping = new HashMap<>();
    static {
        sortMapping.put("origin", "emails");
    }

    private Long id;
    private Long petitionId;
    private String origin;
    private String filename;
    private String date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
