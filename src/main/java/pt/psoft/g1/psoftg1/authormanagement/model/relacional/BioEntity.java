package pt.psoft.g1.psoftg1.authormanagement.model.relacional;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Embeddable

public class BioEntity {


    @Column(nullable = false, length = 4096)
    @NotNull
    @Size(min = 1, max = 4096)
    private String bio;

    public BioEntity() {
        // ORM
    }

    public BioEntity(String bio) {
        setBio(bio);
    }




    public void setBio(String bio) {
        if (bio == null || bio.isBlank() || bio.length() > 4096) {
            throw new IllegalArgumentException("Bio inválida");
        }
        this.bio = bio; // podes aplicar sanitização aqui
    }


    @Override
    public String toString() {
        return bio;

}
}