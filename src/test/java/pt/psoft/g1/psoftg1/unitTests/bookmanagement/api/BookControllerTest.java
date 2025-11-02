package pt.psoft.g1.psoftg1.unitTests.bookmanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookController;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookView;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock private BookService bookService;
    @Mock private LendingService lendingService;
    @Mock private ConcurrencyService concurrencyService;
    @Mock private FileStorageService fileStorageService;
    @Mock private UserService userService;
    @Mock private ReaderService readerService;
    @Mock private BookViewMapper bookViewMapper;

    @InjectMocks
    private BookController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIsbn_shouldReturnBookView() {
        // Arrange
        Book book = mock(Book.class);
        when(book.getVersion()).thenReturn(1L);
        when(bookService.findByIsbn("123")).thenReturn(book);

        BookView bookView = mock(BookView.class);
        when(bookViewMapper.toBookView(book)).thenReturn(bookView);

        // Act
        ResponseEntity<BookView> response = controller.findByIsbn("123");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(bookView, response.getBody());
        verify(bookService).findByIsbn("123");
    }

    @Test
    void create_shouldReturnCreatedResponse() {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setPhoto(null);

        Book createdBook = mock(Book.class);
        when(createdBook.getIsbn()).thenReturn("123");
        when(createdBook.getVersion()).thenReturn(1L);

        when(bookService.create(any(), eq("123"))).thenReturn(createdBook);
        when(bookViewMapper.toBookView((Book) any())).thenReturn(new BookView());

        // Mock do ServletUriComponentsBuilder
        ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
        var uriComponents = mock(org.springframework.web.util.UriComponents.class);

        when(builder.pathSegment(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/books/123"));

        try (MockedStatic<ServletUriComponentsBuilder> mocked = mockStatic(ServletUriComponentsBuilder.class)) {
            mocked.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(builder);

            // Act
            ResponseEntity<BookView> response = controller.create(request, "123");

            // Assert
            assertEquals(201, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            verify(bookService).create(any(), eq("123"));
        }
    }

    @Test
    void findBooks_shouldCombineResults() {
        // Arrange
        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);
        when(b1.getTitle()).thenReturn(new Title("A"));
        when(b2.getTitle()).thenReturn(new Title("B"));

        when(bookService.findByTitle("abc")).thenReturn(List.of(b1));
        when(bookService.findByGenre("fantasy")).thenReturn(List.of(b2));

        when(bookViewMapper.toBookView(anyList())).thenReturn(List.of(new BookView(), new BookView()));

        // Act
        ListResponse<BookView> result = controller.findBooks("abc", "fantasy", null);

        // Assert
        assertEquals(2, result.getItems().size());
        verify(bookService).findByTitle("abc");
        verify(bookService).findByGenre("fantasy");
    }

    @Test
    void updateBook_shouldReturnUpdatedBook() {
        // Arrange
        UpdateBookRequest req = new UpdateBookRequest();
        req.setPhoto(null);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getHeader(ConcurrencyService.IF_MATCH)).thenReturn("1");

        when(concurrencyService.getVersionFromIfMatchHeader("1")).thenReturn(1L);

        Book book = mock(Book.class);
        when(book.getVersion()).thenReturn(2L);
        when(bookService.update(any(), anyString())).thenReturn(book);

        when(bookViewMapper.toBookView(book)).thenReturn(new BookView());

        // Act
        ResponseEntity<BookView> response = controller.updateBook("123", webRequest, req);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(bookService).update(any(), eq("1"));
    }

    // New tests to cover remaining controller methods

    @Test
    void deleteBookPhoto_whenNoPhoto_shouldReturnNotFound() {
        Book book = mock(Book.class);
        when(book.getPhoto()).thenReturn(null);
        when(bookService.findByIsbn("123")).thenReturn(book);

        var response = controller.deleteBookPhoto("123");

        assertEquals(404, response.getStatusCodeValue());
        verify(bookService).findByIsbn("123");
        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    void deleteBookPhoto_whenHasPhoto_shouldDeleteAndReturnOk() {
        Book book = mock(Book.class);
        var photo = mock(pt.psoft.g1.psoftg1.shared.model.Photo.class);
        when(photo.getPhotoFile()).thenReturn("file.png");
        when(book.getPhoto()).thenReturn(photo);
        when(book.getIsbn()).thenReturn("123");
        when(book.getVersion()).thenReturn(1L);
        when(bookService.findByIsbn("123")).thenReturn(book);

        var response = controller.deleteBookPhoto("123");

        assertEquals(200, response.getStatusCodeValue());
        verify(fileStorageService).deleteFile("file.png");
        verify(bookService).removeBookPhoto("123", 1L);
    }

    @Test
    void getSpecificBookPhoto_whenNoPhoto_shouldReturnEmptyOk() {
        Book book = mock(Book.class);
        when(book.getPhoto()).thenReturn(null);
        when(bookService.findByIsbn("123")).thenReturn(book);

        ResponseEntity<byte[]> response = controller.getSpecificBookPhoto("123");

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void getSpecificBookPhoto_whenFileMissing_shouldReturnEmptyOk() {
        Book book = mock(Book.class);
        var photo = mock(pt.psoft.g1.psoftg1.shared.model.Photo.class);
        when(photo.getPhotoFile()).thenReturn("file.png");
        when(book.getPhoto()).thenReturn(photo);
        when(bookService.findByIsbn("123")).thenReturn(book);

        when(fileStorageService.getFile("file.png")).thenReturn(null);
        when(fileStorageService.getExtension("file.png")).thenReturn(Optional.of("png"));

        ResponseEntity<byte[]> response = controller.getSpecificBookPhoto("123");

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void getSpecificBookPhoto_whenFilePresent_shouldReturnImage() {
        Book book = mock(Book.class);
        var photo = mock(pt.psoft.g1.psoftg1.shared.model.Photo.class);
        when(photo.getPhotoFile()).thenReturn("file.png");
        when(book.getPhoto()).thenReturn(photo);
        when(bookService.findByIsbn("123")).thenReturn(book);

        byte[] data = new byte[] {1,2,3};
        when(fileStorageService.getFile("file.png")).thenReturn(data);
        when(fileStorageService.getExtension("file.png")).thenReturn(Optional.of("png"));

        ResponseEntity<byte[]> response = controller.getSpecificBookPhoto("123");

        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(data, response.getBody());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }

    @Test
    void getTop5BooksLent_shouldReturnList() {
        var dto = new BookCountDTO();
        when(bookService.findTop5BooksLent()).thenReturn(List.of(dto));
        when(bookViewMapper.toBookCountView(anyList())).thenReturn(List.of(new pt.psoft.g1.psoftg1.bookmanagement.api.BookCountView()));

        ListResponse<?> resp = controller.getTop5BooksLent();
        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
        verify(bookService).findTop5BooksLent();
    }

    @Test
    void getBooksSuggestions_shouldReturnSuggestions() {
        Authentication auth = mock(Authentication.class);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("u1");
        when(userService.getAuthenticatedUser(auth)).thenReturn(user);

        ReaderDetails rd = mock(ReaderDetails.class);
        when(rd.getReaderNumber()).thenReturn("R1");
        when(readerService.findByUsername("u1")).thenReturn(Optional.of(rd));

        Book b = mock(Book.class);
        when(bookService.getBooksSuggestionsForReader("R1")).thenReturn(List.of(b));
        when(bookViewMapper.toBookView(anyList())).thenReturn(List.of(new BookView()));

        ListResponse<BookView> resp = controller.getBooksSuggestions(auth);
        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
    }

    @Test
    void getAvgLendingDurationByIsbn_shouldReturnView() {
        Book book = mock(Book.class);
        when(bookService.findByIsbn("123")).thenReturn(book);
        when(lendingService.getAvgLendingDurationByIsbn("123")).thenReturn(3.5);
        when(bookViewMapper.toBookAverageLendingDurationView(book, 3.5)).thenReturn(new pt.psoft.g1.psoftg1.bookmanagement.api.BookAverageLendingDurationView());

        ResponseEntity<pt.psoft.g1.psoftg1.bookmanagement.api.BookAverageLendingDurationView> resp = controller.getAvgLendingDurationByIsbn("123");
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        verify(lendingService).getAvgLendingDurationByIsbn("123");
    }

    @Test
    void searchBooks_shouldDelegateToService() {
        SearchRequest<SearchBooksQuery> req = new SearchRequest<>();
        req.setPage(new Page(0,10));
        req.setQuery(new SearchBooksQuery());

        Book b = mock(Book.class);
        when(bookService.searchBooks(req.getPage(), req.getQuery())).thenReturn(List.of(b));
        when(bookViewMapper.toBookView(anyList())).thenReturn(List.of(new BookView()));

        ListResponse<BookView> resp = controller.searchBooks(req);
        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
    }

}
