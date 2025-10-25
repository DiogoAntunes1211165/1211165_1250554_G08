/* pt.psoft.g1.psoftg1.unitTests.authormanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.WebRequest;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorController;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorView;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;
    @MockBean
    private AuthorViewMapper authorViewMapper;
    @MockBean
    private ConcurrencyService concurrencyService;
    @MockBean
    private FileStorageService fileStorageService;
    @MockBean
    private BookViewMapper bookViewMapper;

    private Author author;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    // ----------------------------
    // POST /api/authors
    // ----------------------------
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateAuthor_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "test".getBytes());
        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setPhoto(file);

        Author author = mock(Author.class);

        AuthorView authorView = mock(AuthorView.class);

        when(fileStorageService.getRequestPhoto(any())).thenReturn("photo.jpg");
        when(authorService.create(any())).thenReturn(author);
        when(authorViewMapper.toAuthorView(author)).thenReturn(new AuthorView());


        mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Author Name\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
}
    // ----------------------------
    // PATCH /api/authors/{id}
    // ----------------------------

    /*
    @Test
    void testPartialUpdate_Success() throws Exception {
        when(concurrencyService.getVersionFromIfMatchHeader(any())).thenReturn(1L);
        when(authorService.partialUpdate(eq("A123"), any(UpdateAuthorRequest.class), anyLong()))
                .thenReturn(author);
        when(authorViewMapper.toAuthorView(any())).thenReturn(new AuthorView("A123", "Updated Author", null));

        mockMvc.perform(patch("/api/authors/A123")
                        .header("If-Match", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "1"))
                .andExpect(jsonPath("$.authorNumber").value("A123"));
    }

    // ----------------------------
    // GET /api/authors/{id}
    // ----------------------------
    @Test
    void testGetAuthorByNumber_Success() throws Exception {
        when(authorService.findByAuthorNumber("A123")).thenReturn(Optional.of(author));
        when(authorViewMapper.toAuthorView(any())).thenReturn(new AuthorView("A123", "John Doe", null));

        mockMvc.perform(get("/api/authors/A123"))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "1"))
                .andExpect(jsonPath("$.authorNumber").value("A123"));
    }

    // ----------------------------
    // GET /api/authors?name=...
    // ----------------------------
    @Test
    void testFindByName_ReturnsList() throws Exception {
        when(authorService.findByName("John")).thenReturn(List.of(author));
        when(authorViewMapper.toAuthorView(anyList()))
                .thenReturn(List.of(new AuthorView("A123", "John Doe", null)));

        mockMvc.perform(get("/api/authors?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].authorNumber").value("A123"));
    }

    // ----------------------------
    // GET /api/authors/top5
    // ----------------------------
    @Test
    void testGetTop5Authors_Success() throws Exception {
        AuthorLendingView lendingView = mock(AuthorLendingView.class);
        when(authorService.findTopAuthorByLendings()).thenReturn(List.of(lendingView));

        mockMvc.perform(get("/api/authors/top5"))
                .andExpect(status().isOk());

        verify(authorService).findTopAuthorByLendings();
    }

    // ----------------------------
    // DELETE /api/authors/{id}/photo
    // ----------------------------
    @Test
    void testDeleteAuthorPhoto_Success() throws Exception {
        author.setPhoto(new pt.psoft.g1.psoftg1.shared.model.Photo("photo.jpg"));
        when(authorService.findByAuthorNumber("A123")).thenReturn(Optional.of(author));

        mockMvc.perform(delete("/api/authors/A123/photo"))
                .andExpect(status().isOk());

        verify(fileStorageService).deleteFile("photo.jpg");
        verify(authorService).removeAuthorPhoto(eq("A123"), eq(1L));
    }

    @Test
    void testDeleteAuthorPhoto_NotFound() throws Exception {
        when(authorService.findByAuthorNumber("A123")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/authors/A123/photo"))
                .andExpect(status().isForbidden());
    }
} */
