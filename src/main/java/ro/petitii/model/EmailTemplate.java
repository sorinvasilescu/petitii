package ro.petitii.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.serializers.JsonCategorySerializer;

import javax.persistence.*;

@Entity
@Table(name = "email_templates")
public class EmailTemplate {
    public enum Category {
        response,
        forward,
        start_work
     }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(DataTablesOutput.View.class)
    private Long id;

    @JsonView(DataTablesOutput.View.class)
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Enumerated(EnumType.STRING)
    @JsonSerialize(using = JsonCategorySerializer.class)
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
