package pt.psoft.g1.psoftg1.authormanagement.model.nonrelational;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.EntityWithPhotoDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;

@Document(collection = "authors")
public class AuthorDocument extends EntityWithPhotoDocument {

    @Getter
    @Setter
    @Field("author_id")
    private String authorNumber;

    @Version
    private long version;

    @Getter
    @Setter
    @Field("gen_id")
    private String genId;

    @Getter
    @Field("name")
    private NameDocument name;

    @Getter
    @Field("bio")
    private BioDocument bio;

    @Builder
    public AuthorDocument(String name, String bio, String photoURI, String genId) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
        setGenId(genId);
    }

    protected AuthorDocument() {}

    public String getId() {
        return authorNumber;
    }

    public Long getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = new NameDocument(name);
    }

    public void setBio(String bio) {
        this.bio = new BioDocument(bio);
    }

}
