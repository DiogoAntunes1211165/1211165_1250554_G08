package pt.psoft.g1.psoftg1.unitTests.bookmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.bookmanagement.services.CreateBookRequest;
import pt.psoft.g1.psoftg1.bookmanagement.services.google.IsbnLookupService;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private ReaderRepository readerRepository;
    @Mock private IsbnLookupService isbnLookupService;

    @InjectMocks
    private BookServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Configura lista de lookup services
        setPrivateField("isbnLookupServices", List.of(isbnLookupService));
        // Configura o limite de sugestões
        setPrivateField("suggestionsLimitPerGenre", 2L);
    }

    private void setPrivateField(String name, Object value) {
        try {
            Field f = BookServiceImpl.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- TESTES PARA CREATE ----------

    @Test
    void create_shouldThrow_whenIsbnExists() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Some Title");
        req.setGenre("Fiction");

        when(bookRepository.findByIsbn("1234")).thenReturn(Optional.of(mock(Book.class)));

        assertThatThrownBy(() -> service.create(req, "1234"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_shouldThrow_whenGenreNotFound() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Book");
        req.setGenre("Nonexistent");

        when(bookRepository.findByIsbn("999")).thenReturn(Optional.empty());
        when(genreRepository.findByString("Nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req, "999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Genre not found");
    }

    @Test
    void create_shouldUseIsbnFromLookup_whenIsbnMissing() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("TitleX");
        req.setGenre("Fiction");

        // Ensure the request has at least one author (the service expects a list of author numbers)
        req.setAuthors(List.of(1L));

        // Mock author repository to return an Author for the provided author number
        when(authorRepository.findByAuthorNumber("1")).thenReturn(Optional.of(mock(Author.class)));

        when(isbnLookupService.findIsbnByTitle("TitleX")).thenReturn(Optional.of("9782826012092"));
        when(bookRepository.findByIsbn("9782826012092")).thenReturn(Optional.empty());

        when(genreRepository.findByString("Fiction"))
                .thenReturn(Optional.of(mock(Genre.class)));
        when(bookRepository.save(any(Book.class))).thenReturn(mock(Book.class));

        Book result = service.create(req, null);

        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    // ---------- TESTES PARA UPDATE ----------

    @Test
    void update_shouldThrow_whenGenreInvalid() {
        UpdateBookRequest req = new UpdateBookRequest();
        req.setIsbn("1234");
        req.setGenre("Unknown");

        when(bookRepository.findByIsbn("1234")).thenReturn(Optional.of(mock(Book.class)));
        when(genreRepository.findByString("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(req, "1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Genre not found");
    }

    @Test
    void update_shouldSaveBook_whenValid() {
        UpdateBookRequest req = new UpdateBookRequest();
        req.setIsbn("1234");

        Book book = mock(Book.class);
        when(bookRepository.findByIsbn("1234")).thenReturn(Optional.of(book));

        Book result = service.update(req, "1");

        assertThat(result).isEqualTo(book);
        verify(bookRepository).save(book);
    }

    // ---------- TESTES PARA GETBOOKSUGGESTIONS ----------

    @Test
    void getBooksSuggestions_shouldThrow_whenReaderNotFound() {
        when(readerRepository.findByReaderNumber("r1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getBooksSuggestionsForReader("r1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Reader not found");
    }

    @Test
    void getBooksSuggestions_shouldThrow_whenReaderHasNoInterests() {
        ReaderDetails reader = mock(ReaderDetails.class);
        when(reader.getInterestList()).thenReturn(List.of());
        when(readerRepository.findByReaderNumber("r1")).thenReturn(Optional.of(reader));

        assertThatThrownBy(() -> service.getBooksSuggestionsForReader("r1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no interests");
    }

    @Test
    void getBooksSuggestions_shouldReturnBooks_withLimit() {
        Genre g = mock(Genre.class);
        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);
        ReaderDetails reader = mock(ReaderDetails.class);
        when(reader.getInterestList()).thenReturn(List.of(g));
        when(readerRepository.findByReaderNumber("r1")).thenReturn(Optional.of(reader));
        when(bookRepository.findByGenre(anyString())).thenReturn(List.of(b1, b2, mock(Book.class)));

        List<Book> result = service.getBooksSuggestionsForReader("r1");

        assertThat(result).hasSize(2); // limitado por suggestionsLimitPerGenre
    }

    // ---------- TESTES SIMPLES ----------

    @Test
    void findByIsbn_shouldThrow_whenNotFound() {
        when(bookRepository.findByIsbn("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByIsbn("nope"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByIsbn_shouldReturnBook_whenFound() {
        Book book = mock(Book.class);
        when(bookRepository.findByIsbn("ok")).thenReturn(Optional.of(book));
        assertThat(service.findByIsbn("ok")).isEqualTo(book);
    }

    @Test
    void searchBooks_shouldUseDefaults_whenNullArgs() {
        when(bookRepository.searchBooks(any(Page.class), any(SearchBooksQuery.class)))
                .thenReturn(List.of(mock(Book.class)));
        var result = service.searchBooks(null, null);
        assertThat(result).hasSize(1);
    }
}
