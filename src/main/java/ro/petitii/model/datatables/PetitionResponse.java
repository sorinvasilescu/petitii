package ro.petitii.model.datatables;

import java.util.HashMap;
import java.util.Map;

public class PetitionResponse {
    public static final Map<String, String> sortMapping = new HashMap<>();
    static {
        sortMapping.put("petitionerEmail", "petitioner");
        sortMapping.put("status", "currentStatus");
    }

    private Long id;
    private String regNo;
    private String petitionerName;
    private String receivedDate;
    private String lastUpdateDate;
    private String user;
    private String petitionerEmail;
    private String subject;
    private String status;
    private String deadline;
    private String alertStatus;

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

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPetitionerEmail() {
        return petitionerEmail;
    }

    public void setPetitionerEmail(String petitionerEmail) {
        this.petitionerEmail = petitionerEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }
}
