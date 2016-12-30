package ro.petitii.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by mpostelnicu on 12/23/2016.
 */
@Entity
@Table(name = "email_templates")
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(DataTablesOutput.View.class)
    private Long id;

    @JsonView(DataTablesOutput.View.class)
    private String name;

    @Lob
    @Column(name="CONTENT")
    @JsonView(DataTablesOutput.View.class)
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
