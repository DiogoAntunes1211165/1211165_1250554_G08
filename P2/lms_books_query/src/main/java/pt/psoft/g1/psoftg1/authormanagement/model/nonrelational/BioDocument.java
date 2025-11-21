package pt.psoft.g1.psoftg1.authormanagement.model.nonrelational;

import lombok.Getter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.shared.model.StringUtilsCustom;

@Getter
public class BioDocument {

    @Transient
    private final int BIO_MAX_LENGTH = 4096;

    @Field("bio")
    private String bio;

    public BioDocument(String bio) {
        setBio(bio);
    }

    // Construtor vazio obrigatório para o mapeamento MongoDB
    protected BioDocument() {
    }

    public void setBio(String bio) {
        if (bio == null)
            throw new IllegalArgumentException("Bio cannot be null");
        if (bio.isBlank())
            throw new IllegalArgumentException("Bio cannot be blank");
        if (bio.length() > BIO_MAX_LENGTH)
            throw new IllegalArgumentException("Bio has a maximum of 4096 characters");
        this.bio = StringUtilsCustom.sanitizeHtml(bio);
    }

    @Override
    public String toString() {
        return this.bio;
    }
}
