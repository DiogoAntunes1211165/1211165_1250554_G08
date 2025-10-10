package pt.psoft.g1.psoftg1.authormanagement.model.relacional;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.relational.EntityWithPhotoEntity;
import pt.psoft.g1.psoftg1.shared.model.relational.NameEntity;

@Entity
@Table(name = "Author")
public class AuthorEntity extends EntityWithPhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUTHOR_NUMBER")

    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Embedded
    private NameEntity name;

    @Embedded
    private BioEntity bio;

    public void setName(String name) {
        this.name = new NameEntity(name);
    }

    public void setBio(BioEntity bio) {
        this.bio = bio;
    }

    public long getVersion() {
        return version;
    }




    @Builder
    public AuthorEntity(String name, BioEntity bio, String photoURI) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
    }

    protected AuthorEntity() {
        // ORM
    }


    public String getName() {
        return this.name.toString();
    }


    public String getBio() {
        return this.bio.toString();
    }









}
