package ro.petitii.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import javax.persistence.*;

import static ro.petitii.util.TranslationUtil.categoryMsg;

@Entity
@Table(name = "email_templates")
public class EmailTemplate {
    public enum Category {
        response,
        forward,
        start_work,
        recover_password;

        public String viewName() {
            return categoryMsg(this);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(DataTablesOutput.View.class)
    private Long id;

    @JsonView(DataTablesOutput.View.class)
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Enumerated(EnumType.STRING)
    private Category category;

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
