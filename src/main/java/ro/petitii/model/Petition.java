package ro.petitii.model;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
public class Petition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // todo relation with registration number table
    private Long regNo;

    private Date receivedDate;
    private String relation;

    // todo relation with petitioner table
    private Long petitioner;

    private String origin;
    private String type;
    private String field;
    private String _abstract;
    private String description;

    // todo relation with users table
    private Long responsible;

    @OneToMany(mappedBy = "petition")
    private Collection<Email> emails;

    public Long getId() {
        return id;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<Email> getEmails() {
        return emails;
    }

    public void setEmails(Collection<Email> emails) {
        this.emails = emails;
    }
}
