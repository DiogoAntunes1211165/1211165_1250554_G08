package pt.psoft.g1.psoftg1.integrationTests.authormanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorController;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorView;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
class AuthorControllerOpaqueTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private AuthorViewMapper authorViewMapper;

    @MockBean
    private BookViewMapper bookViewMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ConcurrencyService concurrencyService;

    @Test
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN"})
    void testCreateAuthorWithoutPhoto() throws Exception {
        // Mock Author
        Author mockedAuthor = Mockito.mock(Author.class);
        when(mockedAuthor.getVersion()).thenReturn(1L);

        // Mock Service
        when(authorService.create(any())).thenReturn(mockedAuthor);

        // Mock Mapper
        AuthorView view = new AuthorView();
        view.setAuthorNumber(1L);
        view.setName("John Doe");
        view.setBio(null);
        when(authorViewMapper.toAuthorView(mockedAuthor)).thenReturn(view);

        // Multipart file (empty photo)
        MockMultipartFile photoFile = new MockMultipartFile("photo", new byte[0]);

        mockMvc.perform(multipart("/api/authors")
                        .file(photoFile)
                        .param("name", "John Doe")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.authorNumber").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetAuthorNotFound() throws Exception {
        when(authorService.findByAuthorNumber(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/authors/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetAuthorPhotoNotFound() throws Exception {
        Author mockedAuthor = Mockito.mock(Author.class);
        when(mockedAuthor.getPhoto()).thenReturn(null); // Sem foto
        when(authorService.findByAuthorNumber(anyString())).thenReturn(Optional.of(mockedAuthor));

        mockMvc.perform(get("/api/authors/123/photo"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN"})
    void testDeleteAuthorPhotoNotFound() throws Exception {
        when(authorService.findByAuthorNumber(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/authors/123/photo").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetTop5AuthorsNotFound() throws Exception {
        when(authorService.findTopAuthorByLendings()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/authors/top5"))
                .andExpect(status().isNotFound());
    }
}
