package ro.petitii.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "petition_custom_params_values")
public class PetitionCustomParamValue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @ManyToOne
    @JoinColumn(name = "cp_id")
    @JsonIgnore
    private PetitionCustomParam param;

    private String label;
    private String value;

    public PetitionCustomParamValue() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PetitionCustomParam getParam() {
        return param;
    }

    public void setParam(PetitionCustomParam param) {
        this.param = param;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PetitionCustomParamValue{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
