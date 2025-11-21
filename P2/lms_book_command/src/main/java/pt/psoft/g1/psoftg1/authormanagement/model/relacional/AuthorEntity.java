package pt.psoft.g1.psoftg1.authormanagement.model.relacional;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.shared.model.relational.EntityWithPhotoEntity;
import pt.psoft.g1.psoftg1.shared.model.relational.NameEntity;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "Author")
public class AuthorEntity extends EntityWithPhotoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUTHOR_NUMBER")
    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Getter
    @Setter
    private String genId;

    @Embedded
    private NameEntity name;

    @Embedded
    private BioEntity bio;

    public void setName(String name) {
        this.name = new NameEntity(name);
    }

    public void setBio(String bio) {
        this.bio = new BioEntity(bio);
    }

    public Long getVersion() {
        return version;
    }

    public Long getId() {
        return authorNumber;
    }



    @Builder
    public AuthorEntity(String name, String bio, String photoURI, String genId) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
        setGenId(genId);
    }

    protected AuthorEntity() {
        // got ORM only
    }
    public String getName() {
        return this.name.toString();
    }

    public String getBio() {
        return this.bio.toString();
    }
}