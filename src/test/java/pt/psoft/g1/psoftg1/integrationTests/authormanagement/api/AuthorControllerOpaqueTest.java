package pt.psoft.g1.psoftg1.integrationTests.authormanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthorControllerOpaqueTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void testFindByAuthorNumber_Success() throws Exception {
        // Arrange: Insert a new author into the real database
        Author author = new Author("John Doe", "A famous author", null, null);
        Author savedAuth = authorRepository.save(author);

        // Act: Perform the GET request on the author
        mockMvc.perform(get("/api/authors/{authorNumber}", 4052)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("A famous author"));
    }



    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void testFindByAuthorNumber_NotFound() throws Exception {
        // Act and Assert: Test for a non-existent author (should return 404)
        mockMvc.perform(get("/api/authors/{authorNumber}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void testFindByName_Success() throws Exception {

        // Act: Perform a GET request for authors by name should not be found
        mockMvc.perform(get("/api/authors")
                        .param("name", "Jane")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());


        // Arrange: Insert an author with a specific name
        Author author = new Author("Jane Doe", "A well-known author", "photo.jpg", null);
        authorRepository.save(author);



        // Act: Perform a GET request for authors by name
        mockMvc.perform(get("/api/authors")
                        .param("name", "Jane")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$.items[0].bio").value("A well-known author"))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"LIBRARIAN"})
    @Rollback
    public void testFindByName_NotFound() throws Exception {
        // Act and Assert: Test for a non-existent author name (expect an empty list)
        mockMvc.perform(get("/api/authors")
                        .param("name", "NonExistentAuthor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Should return 200 but with an empty list
                .andExpect(jsonPath("$.items").isEmpty());
    }



    @Test
    @WithMockUser(username = "admin", roles = {"READER"})
    @Rollback
    public void testGetBooksByAuthorNumber_AuthorNotFound() throws Exception {
        // Act: Perform the GET request for books by a non-existent author number
        mockMvc.perform(get("/api/authors/{authorNumber}/books", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }



}
