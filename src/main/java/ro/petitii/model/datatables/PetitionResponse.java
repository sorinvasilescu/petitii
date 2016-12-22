package ro.petitii.model.datatables;

public class PetitionResponse {
    private Long id;
    private String regNo;
    private String petitionerName;
    private String petitionerEmail;
    private String _abstract;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getPetitionerName() {
        return petitionerName;
    }

    public void setPetitionerName(String petitionerName) {
        this.petitionerName = petitionerName;
    }

    public String getPetitionerEmail() {
        return petitionerEmail;
    }

    public void setPetitionerEmail(String petitionerEmail) {
        this.petitionerEmail = petitionerEmail;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
