package pt.psoft.g1.psoftg1.genremanagement.model.nonrelational;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "genres")
public class GenreDocument {

    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @Field("genre")
    private String genre;

    public GenreDocument() {}

    public GenreDocument(String genre) {
        this.genre = genre;
    }
}
