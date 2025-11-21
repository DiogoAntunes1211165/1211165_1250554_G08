package pt.psoft.g1.psoftg1.authormanagement.model.relacional;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.psoft.g1.psoftg1.shared.model.StringUtilsCustom;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
public class BioEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Transient
    private final int BIO_MAX_LENGTH = 4096;

    @Column(nullable = false, length = BIO_MAX_LENGTH)
    @NotNull
    @Size(min = 1, max = BIO_MAX_LENGTH)
    private String bio;

    public BioEntity(String bio) {
        setBio(bio);
    }

    protected BioEntity() {
    }

    public void setBio(String bio) {
        if(bio == null)
            throw new IllegalArgumentException("Bio cannot be null");
        if(bio.isBlank())
            throw new IllegalArgumentException("Bio cannot be blank");
        if(bio.length() > BIO_MAX_LENGTH)
            throw new IllegalArgumentException("Bio has a maximum of 4096 characters");
        this.bio = StringUtilsCustom.sanitizeHtml(bio);
    }

    @Override
    public String toString() {
        return bio;
    }
}