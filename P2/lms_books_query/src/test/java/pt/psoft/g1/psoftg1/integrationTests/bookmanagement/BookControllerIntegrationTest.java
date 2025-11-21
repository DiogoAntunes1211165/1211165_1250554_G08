package pt.psoft.g1.psoftg1.integrationTests.bookmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
//    @Rollback
//    public void testCreateBook_Success() throws Exception {
//
//        // Create a new Genre:
//        Genre genre = genreRepository.save(new Genre("Programming"));
//
//        // Create a new Author:
//        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
//
//        authorRepository.findByAuthorNumber(author.getAuthorNumber());
//        System.out.println(authorRepository.findByAuthorNumber(author.getAuthorNumber()));
//
//        String isbn = "9789720706386";
//
//        // Build the request payload as a JSON string
//        String requestBody = String.format("""
//            {
//                "title": "Invisible Book",
//                "genre": "%s",
//                "authors": [%d],
//                "description": "This is a book that you can't see",
//                "photoURI": null,
//                "photo": null
//            }
//            """, genre.getGenre(), Long.parseLong(author.getAuthorNumber()));
//
//        // Act: Perform the PUT request to create a new book with the provided ISBN
//        mockMvc.perform(put("/api/books/{isbn}", isbn)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isCreated());
//
//    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByIsbn() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";

        // Create a new Book:
        bookRepository.save(new Book(isbn, "Clean Code", "Robert C Martin", genre, authors, null));

        // Act: Perform the PUT request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books/{isbn}", isbn)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByIsbn_NotFound() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";

        // Create a new Book:
        bookRepository.save(new Book(isbn, "Clean Code", "Robert C Martin", genre, authors, null));

        // Act: Perform the PUT request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books/{isbn}", "9789720706381")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByGenre() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";

        // Create a new Book:
        bookRepository.save(new Book(isbn, "Clean Code", "Robert C Martin", genre, authors, null));


        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("genre", genre.getGenre())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByGenre_NotFound() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";

        // Create a new Book:
        bookRepository.save(new Book(isbn, "Clean Code", "Robert C Martin", genre, authors, null));


        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("genre", "Sports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByTitle() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";
        String title = "Clean Code";

        // Create a new Book:
        bookRepository.save(new Book(isbn, title, "Robert C Martin", genre, authors, null));



        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("title", title)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByTitle_NotFound() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";
        String title = "Clean Code";

        // Create a new Book:
        bookRepository.save(new Book(isbn, title, "Robert C Martin", genre, authors, null));



        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("title", "The Art of Formula 1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByAuthorName() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";
        String title = "Clean Code";

        // Create a new Book:
        bookRepository.save(new Book(isbn, title, "Robert C Martin", genre, authors, null));



        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("authorName", author.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void findByAuthorName_NotFound() throws Exception {

        // Create a new Genre:
        Genre genre = genreRepository.save(new Genre("Programming"));

        // Create a new Author:
        Author author = authorRepository.save(new Author("Robert C Martin", "A famous author", null, null));
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        String isbn = "9789720706386";
        String title = "Clean Code";

        // Create a new Book:
        bookRepository.save(new Book(isbn, title, "Robert C Martin", genre, authors, null));



        // Act: Perform the GET request to create a new book with the provided ISBN
        mockMvc.perform(get("/api/books")
                        .param("authorName", "Lewis Hamilton")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}