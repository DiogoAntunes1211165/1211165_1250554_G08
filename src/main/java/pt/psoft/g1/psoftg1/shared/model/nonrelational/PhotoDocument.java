package pt.psoft.g1.psoftg1.shared.model.nonrelational;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.nio.file.Path;

@Document(collection = "photos")
public class PhotoDocument {

    @Id
    private String id;

    @NotNull
    @Setter
    @Getter
    @Field("photo_file")
    private String photoFile;

    public PhotoDocument() {
    }

    public PhotoDocument(Path photoPath) {
        setPhotoFile(photoPath.toString());
    }
}

