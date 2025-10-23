package pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.AuthorDocument;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.EntityWithPhotoDocument;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "books")
public class BookDocument extends EntityWithPhotoDocument {

    @Id
    private String id;

    @Version
    private Long version;

    @Getter @Setter
    @Field("isbn")
    IsbnDocument isbn;

    @Getter @Setter
    @NotNull
    @Field("title")
    TitleDocument title;

    @Getter @Setter
    @NotNull
    @DBRef
    @Field("genre")
    GenreDocument genre;

    @Getter @Setter
    @Field("authors")
    @DBRef
    private List<AuthorDocument> authors = new ArrayList<>();

    DescriptionDocument description;

    public BookDocument() {}

}
