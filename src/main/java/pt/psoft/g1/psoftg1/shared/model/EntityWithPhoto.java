package pt.psoft.g1.psoftg1.shared.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Getter

public abstract class EntityWithPhoto {

    protected Photo photo;

    //This method is used by the mapper in order to set the photo. This will call the setPhotoInternal method that
    //will contain all the logic to set the photo
    public void setPhoto(String photoUri) {
        this.setPhotoInternal(photoUri);
    }

    protected void setPhotoInternal(String photoURI) {
        if (photoURI == null) {
            this.photo = null;
        } else {
            try {
                Path path = Path.of(photoURI);
                if (this.photo == null) {
                    this.photo = new Photo(path); // só cria novo se não existir
                } else {
                    this.photo.setPhotoFile(photoURI); // atualiza o existente
                }
            } catch (InvalidPathException e) {
                this.photo = null;
            }
        }
    }
}
