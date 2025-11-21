package pt.psoft.g1.psoftg1.authormanagement.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.services.generator.IdGeneratorFactory;


public class Author extends EntityWithPhoto {

    private final IdGeneratorFactory idGeneratorFactory = new IdGeneratorFactory();

    @Getter
    @Setter
    private String authorNumber;

    private long version;


    private Name name;

    @Getter
    private String genId;


    private Bio bio;

    protected Author() {

    }

    public void setGenId(String genId) {
        if (this.genId == null) {
            this.genId = idGeneratorFactory.getGenerator().generateId();
        }else {
            this.genId = genId;
        }
    }

    public void setName(String name) {
        this.name = new Name(name);
    }

    public void setBio(String bio) {
        this.bio = new Bio(bio);
    }

    public Long getVersion() {
        return version;
    }

    public String getId() {
        return genId;
    }

    public Author(String name, String bio, String photoURI, String genId) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
        setGenId(genId);
    }



    public void applyPatch(final long desiredVersion, final String name, final String bio, final String photoURI) {
        if (this.version != desiredVersion)
            throw new StaleObjectStateException("Object was already modified by another user", this.authorNumber);
        if (name != null)
            setName(name);
        if (bio != null)
            setBio(bio);
        if(photoURI != null)
            setPhotoInternal(photoURI);
    }

    public void removePhoto(long desiredVersion) {
        if(desiredVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        setPhotoInternal(null);
    }
    public String getName() {
        return this.name.toString();
    }

    public String getBio() {
        return this.bio.toString();
    }

    public String getPhotoURI() {
        if (super.getPhoto() == null) {
            return null;
        }
        return super.getPhoto().getPhotoFile();
    }


}

