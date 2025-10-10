package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    private Isbn isbn;
    private final String validIsbn = "9782826012092";
    private Title title;
    private final String validTitle = "Encantos de contar";
    private Description description;
    private final String validDescription = "Um livro cheio de encantos e histórias para contar.";
    private final String validAuthor1 = "João Alberto";
    private final String validAuthor1Bio = "O João Alberto nasceu em Chaves e foi pedreiro a maior parte da sua vida.";
    private final String validAuthor2 = "Maria José";
    private final String validAuthor2Bio = "A Maria José nasceu em Viseu e só come laranjas às segundas feiras.";
    private final String validGenre = "Fantasia";
    private ArrayList<Author> authors;
    private Genre genre;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp(){
        // Stub de ISBN
        isbn = Mockito.mock(Isbn.class);
        Mockito.when(isbn.toString()).thenReturn(validIsbn);

        // Stub de título
        title = Mockito.mock(Title.class);
        Mockito.when(title.toString()).thenReturn(validTitle);

        // Stub de descrição
        description = Mockito.mock(Description.class);
        Mockito.when(description.toString()).thenReturn(validDescription);

        // Stub de autores
        Author author1 = Mockito.mock(Author.class);
        Mockito.when(author1.getName()).thenReturn(validAuthor1);
        Mockito.when(author1.getBio()).thenReturn(validAuthor1Bio);

        Author author2 = Mockito.mock(Author.class);
        Mockito.when(author2.getName()).thenReturn(validAuthor2);
        Mockito.when(author2.getBio()).thenReturn(validAuthor2Bio);

        authors = new ArrayList<>();
        authors.add(author1);
        authors.add(author2);

        // Stub de gênero
        genre = Mockito.mock(Genre.class);
        Mockito.when(genre.toString()).thenReturn(validGenre);

        //Stub do UpdateBookRequest
        updateBookRequest = Mockito.mock(UpdateBookRequest.class);
    }

    @Test
    void ensureBookCreatedWithValidParameters() {
        assertDoesNotThrow(() -> new Book(validIsbn, validTitle, validDescription, genre, authors, null));
    }

    @Test
    void ensureIsbnNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Book(null, validTitle, null, genre, authors, null));
    }

    @Test
    void ensureThatIsbnIsGettable(){
        Book book = new Book(validIsbn, validTitle, null, genre, authors, null);
        assertEquals(validIsbn, book.getIsbn());
    }

    @Test
    void ensureThatDescriptionIsGettable(){
        Book book = new Book(validIsbn, validTitle, validDescription, genre, authors, null);
        assertEquals(validDescription, book.getDescription());
    }

    @Test
    void ensureTitleNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Book(validIsbn, null, null, genre, authors, null));
    }

    @Test
    void ensureGenreNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Book(validIsbn, validTitle, null,null, authors, null));
    }

    @Test
    void ensureAuthorsNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Book(validIsbn, validTitle, null, genre, null, null));
    }

    @Test
    void ensureAuthorsNotEmpty(){
        authors.clear();
        assertThrows(IllegalArgumentException.class, () -> new Book(validIsbn, validTitle, null, genre, authors, null));
    }

    @Test
    void ensureBookCreatedWithMultipleAuthors() {
        assertDoesNotThrow(() -> new Book(validIsbn, validTitle, null, genre, authors, null));
    }

}