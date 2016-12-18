package ro.petitii.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Collection;

@Entity
@Table(name = "petitioners")
public class Petitioner {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Size(min = 1)
    private String firstName;

    @Size(min = 1)
    private String lastName;

    private String organization;
    private String entity_type;

    @Size(min = 1)
    private String email;

    private String phone;
    private String country;
    private String county;
    private String city;
    private String address;
    private String title;

    @OneToMany(mappedBy = "petitioner")
    Collection<Petition> petitions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getEntity_type() {
        return entity_type;
    }

    public void setEntity_type(String entity_type) {
        this.entity_type = entity_type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<Petition> getPetitions() {
        return petitions;
    }

    @Override
    public String toString() {
        return "Petitioner{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", organization='" + organization + '\'' +
                ", entity_type='" + entity_type + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", country='" + country + '\'' +
                ", county='" + county + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder(17,23);
        hb
            .append(firstName)
            .append(lastName)
            .append(organization)
            .append(email)
            .append(title);
        return hb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Petitioner)) return false;
        if (obj == this) return true;
        Petitioner p = (Petitioner) obj;
        EqualsBuilder eb = new EqualsBuilder();
        eb
            .append(firstName, p.firstName)
            .append(lastName, p.lastName)
            .append(organization, p.organization)
            .append(email, p.email)
            .append(title, p.title);
        return eb.isEquals();
    }
}