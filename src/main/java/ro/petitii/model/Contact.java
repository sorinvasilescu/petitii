package ro.petitii.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(DataTablesOutput.View.class)
    private Long id;
    
    @JsonView(DataTablesOutput.View.class)
    String name;
    @JsonView(DataTablesOutput.View.class)
    String phone;
    
    @NotEmpty
    @Email
    @JsonView(DataTablesOutput.View.class)
    String email;

    public Contact(){}
    
    public Contact(long id){
    	this.id = id;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("id:").append(getId());
		builder.append("\nname:").append(getName());
		builder.append("\nemail:").append(getEmail());
		builder.append("\nphone:").append(getPhone());
		return builder.toString();
	};
}
