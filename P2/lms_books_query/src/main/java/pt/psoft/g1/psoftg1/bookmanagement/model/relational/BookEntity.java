package pt.psoft.g1.psoftg1.bookmanagement.model.relational;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.StaleObjectStateException;

import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Book", uniqueConstraints = {
        @UniqueConstraint(name = "uc_book_isbn", columnNames = {"ISBN"})
})
public class BookEntity extends EntityWithPhoto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long pk;

    @Version
    @Getter
    private Long version;

    @Embedded
    IsbnEntity isbn;

    @Getter
    @Embedded
    @NotNull
    TitleEntity title;

    @Getter
    @ManyToOne
    @NotNull
    GenreEntity genre;

    @Getter
    @ManyToMany
    private List<AuthorEntity> authors = new ArrayList<>();

    @Embedded
    DescriptionEntity description;

    private void setTitle(String title) {this.title = new TitleEntity(title);}

    private void setIsbn(String isbn) {
        this.isbn = new IsbnEntity(isbn);
    }

    private void setDescription(String description) {this.description = new DescriptionEntity(description); }

    public void setGenre(GenreEntity genre) {this.genre = genre; }

    public void setAuthors(List<AuthorEntity> authors) {this.authors = authors; }

    public String getDescription(){ return this.description.toString(); }


    public BookEntity(String isbn, String title, String description, GenreEntity genre, List<AuthorEntity> authors, String photoURI) {
        setTitle(title);
        setIsbn(isbn);
        if(description != null)
            setDescription(description);
        if(genre==null)
            throw new IllegalArgumentException("Genre cannot be null");
        setGenre(genre);
        if(authors == null)
            throw new IllegalArgumentException("Author list is null");
        if(authors.isEmpty())
            throw new IllegalArgumentException("Author list is empty");

        setAuthors(authors);
        setPhotoInternal(photoURI);
    }

    protected BookEntity() {
        // got ORM only
    }

    public String getIsbn(){
        return this.isbn.toString();
    }
}