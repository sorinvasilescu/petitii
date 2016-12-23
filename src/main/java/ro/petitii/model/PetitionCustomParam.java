package ro.petitii.model;

import javax.persistence.*;
import java.util.List;

import static ro.petitii.model.PetitionCustomParamType.getParamType;

@Entity
@Table(name = "petition_custom_params")
public class PetitionCustomParam {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String param;

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

    public PetitionCustomParamType getParam() {
        return getParamType(param);
    }

    public void setParam(PetitionCustomParamType param) {
        this.param = param.getDbName();
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
