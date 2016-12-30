package ro.petitii.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "petition_custom_params")
public class PetitionCustomParam {
    public enum Type {
        entity,
        information,
        problem,
        domain,
        title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(value = EnumType.STRING)
    private Type param;

    @Column(name = "friendly_name")
    private String friendlyName;

    @OneToMany
    @JoinColumn(name = "cp_id")
    private List<PetitionCustomParamValue> paramValues;

    @Column(name = "default_value")
    private String defaultValue;

    private boolean required;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getParam() {
        return param;
    }

    public void setParam(Type param) {
        this.param = param;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public List<PetitionCustomParamValue> getParamValues() {
        return paramValues;
    }

    public void setParamValues(List<PetitionCustomParamValue> paramValues) {
        this.paramValues = paramValues;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "PetitionCustomParam{" +
                "id=" + id +
                ", param=" + param +
                ", name=" + friendlyName +
                ", paramValues=" + paramValues +
                ", defaultValue='" + defaultValue + '\'' +
                ", required=" + required +
                '}';
    }
}
