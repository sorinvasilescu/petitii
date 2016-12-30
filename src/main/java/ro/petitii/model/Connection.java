package ro.petitii.model;

import javax.persistence.*;

@Entity
@Table(name = "connections")
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "old_petition_id")
    private Petition oldPetition;

    @OneToOne
    @JoinColumn(name = "new_petition_id")
    private Petition newPetition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Petition getOldPetition() {
        return oldPetition;
    }

    public void setOldPetition(Petition oldPetition) {
        this.oldPetition = oldPetition;
    }

    public Petition getNewPetition() {
        return newPetition;
    }

    public void setNewPetition(Petition newPetition) {
        this.newPetition = newPetition;
    }
}