package pt.psoft.g1.psoftg1.unitTests.authormanagement.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorController;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorView;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.services.*;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorControllerTest {

    @InjectMocks
    private AuthorController authorController;

    @Mock
    private AuthorService authorService;

    @Mock
    private AuthorViewMapper authorViewMapper;

    @Mock
    private ConcurrencyService concurrencyService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private BookViewMapper bookViewMapper;

    @Mock
    private WebRequest webRequest;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        // Ensure ServletUriComponentsBuilder can access the current request
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testCreateAuthor_shouldReturnCreated() {
        // Arrange
        CreateAuthorRequest request = new CreateAuthorRequest();
        MockMultipartFile file = new MockMultipartFile("photo", new byte[0]);
        request.setPhoto(file);

        when(fileStorageService.getRequestPhoto(file)).thenReturn("photo.png");

        // Author has a protected constructor and no setVersion method, use a mock instead
        Author author = mock(Author.class);
        when(author.getVersion()).thenReturn(1L);
        when(author.getAuthorNumber()).thenReturn("123");

        when(authorService.create(any(CreateAuthorRequest.class))).thenReturn(author);
        when(authorViewMapper.toAuthorView(author)).thenReturn(new AuthorView());

        // Act
        // Ensure request attributes are present right before calling controller
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        ResponseEntity<AuthorView> response = authorController.create(request);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testPartialUpdate_shouldReturnOk() {
        // Arrange
        UpdateAuthorRequest request = new UpdateAuthorRequest();
        MockMultipartFile file = new MockMultipartFile("photo", new byte[0]);
        request.setPhoto(file);

        // Use a mocked Author to control getVersion()
        Author author = mock(Author.class);
        when(author.getVersion()).thenReturn(2L);

        // Controller expects header name ConcurrencyService.IF_MATCH which is "If-Match"
        when(webRequest.getHeader("If-Match")).thenReturn("2");
        when(fileStorageService.getRequestPhoto(file)).thenReturn("photo.png");
        when(concurrencyService.getVersionFromIfMatchHeader("2")).thenReturn(2L);
        when(authorService.partialUpdate(anyString(), any(UpdateAuthorRequest.class), eq(2L))).thenReturn(author);
        when(authorViewMapper.toAuthorView(author)).thenReturn(new AuthorView());

        // Act
        ResponseEntity<AuthorView> response = authorController.partialUpdate("123", webRequest, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testFindByAuthorNumber_shouldReturnAuthor() {
        // Arrange
        // Mock Author instead of instantiating
        Author author = mock(Author.class);
        when(author.getAuthorNumber()).thenReturn("123");
        when(author.getVersion()).thenReturn(1L);

        when(authorService.findByAuthorNumber("123")).thenReturn(Optional.of(author));
        when(authorViewMapper.toAuthorView(author)).thenReturn(new AuthorView());

        // Act
        ResponseEntity<AuthorView> response = authorController.findByAuthorNumber("123");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
