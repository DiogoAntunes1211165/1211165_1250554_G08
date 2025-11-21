package pt.psoft.g1.psoftg1.integrationTests.authormanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // garante rollback após cada teste
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void cleanDatabase() {
        // AuthorRepository interface doesn't expose deleteAll(), iterate and delete each element instead
        for (Author a : authorRepository.findAll()) {
            authorRepository.delete(a);
        }
    }

    @Test
    void testCreateAndFindAuthor() {
        // --- 1. Criar autor ---
        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setName("John Doe");
        request.setBio("Test bio");

        Author created = authorService.create(request);
        assertNotNull(created);
        assertEquals("John Doe", created.getName());

        // --- 2. Buscar autor pelo número ---
        Optional<Author> found = authorService.findByAuthorNumber(created.getAuthorNumber());
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testPartialUpdateAuthor() {
        // Criar autor inicial
        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setName("Initial Name");
        request.setBio("Initial Bio");
        Author author = authorService.create(request);

        // Atualizar nome e bio
        UpdateAuthorRequest update = new UpdateAuthorRequest();
        update.setName("Updated Name");
        update.setBio("Updated Bio");

        Author updated = authorService.partialUpdate(author.getAuthorNumber(), update, author.getVersion());
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Bio", updated.getBio());
    }

    /*@Test
    void testFindByName() {
        // Criar vários autores
        CreateAuthorRequest a1 = new CreateAuthorRequest();
        a1.setName("Alice Walker");
        authorService.create(a1);

        CreateAuthorRequest a2 = new CreateAuthorRequest();
        a2.setName("Alan Turing");
        authorService.create(a2);

        List<Author> results = authorService.findByName("Al");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(a -> a.getName().startsWith("Al")));
    } */


    @Test
    void testRemoveAuthorPhoto_ThrowsNotFound() {
        // Não há autor, deve lançar NotFoundException
        assertThrows(RuntimeException.class, () ->
                authorService.removeAuthorPhoto("9999", 1L));
    }
}
