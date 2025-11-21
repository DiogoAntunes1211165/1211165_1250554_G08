package pt.psoft.g1.psoftg1.unitTests.authormanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorMapper;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorMapper authorMapper;
    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private pt.psoft.g1.psoftg1.authormanagement.services.AuthorServiceImpl authorService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------
    // findAll
    // -------------------------------
    @Test
    void testFindAll_ReturnsAllAuthors() {
        Author author = mock(Author.class);
        List<Author> authors = List.of(author);
        when(authorRepository.findAll()).thenReturn(authors);

        Iterable<Author> result = authorService.findAll();

        assertEquals(authors, result);
        verify(authorRepository).findAll();
    }

    // -------------------------------
    // findByAuthorNumber
    // -------------------------------
    @Test
    void testFindByAuthorNumber_Found() {
        // Preparação: definimos o identificador do autor que vamos pesquisar no repositório.
        String authorNumber = "A123";

        // Criamos um mock de Author em vez de instanciar a classe real.
        // Usamos mock quando só nos interessa o comportamento (presença) do objeto,
        // e não a sua implementação concreta.
        Author author = mock(Author.class);

        // Configuração do stub: quando o repository for chamado com "A123", devolve um Optional contendo o mock.
        // Isto simula o comportamento do repositório (BD) sem aceder a uma base de dados real.
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.of(author));

        // Execução: chamamos o método do serviço que estamos a testar.
        // Espera-se que o serviço delegue no repository e devolva o Optional com o author.
        Optional<Author> result = authorService.findByAuthorNumber(authorNumber);

        // Verificação 1: certificamo-nos que o resultado não está vazio (o autor foi encontrado).
        assertTrue(result.isPresent());

        // Verificação 2: garantimos que o author devolvido é exactamente o mock que configurámos.
        // Isto valida que o serviço devolve aquilo que o repository devolve (sem alterar o objecto).
        assertEquals(author, result.get());

        // Verificação interacção: verificamos que o repository foi efectivamente chamado
        // com o argumento esperado (authorNumber). Isto assegura que o serviço fez a chamada correta.
        verify(authorRepository).findByAuthorNumber(authorNumber);
    }


    @Test
    void testFindByAuthorNumber_NotFound() {
        String authorNumber = "A123";
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.findByAuthorNumber("A123");

        assertTrue(result.isEmpty());
    }

    // -------------------------------
    // create
    // -------------------------------
    @Test
    void testCreate_WithValidData() {
        CreateAuthorRequest request = mock(CreateAuthorRequest.class); // Mock do request
        Author author = mock(Author.class);

        when(authorMapper.create(request)).thenReturn(author);
        when(authorRepository.save(author)).thenReturn(author);

        Author result = authorService.create(request);

        assertEquals(author, result);
        verify(authorRepository).save(author);
        verify(authorRepository).save(author);
    }

    // -------------------------------
    // partialUpdate
    // -------------------------------
    @Test
    void testPartialUpdate_Success() {
        Author author = mock(Author.class);
        UpdateAuthorRequest request = mock(UpdateAuthorRequest.class);
        when(authorRepository.findByAuthorNumber("A123")).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);

        Author result = authorService.partialUpdate("A123", request, 2L);

        assertNotNull(result);
        verify(author).applyPatch(2L, request);
        verify(authorRepository).save(author);
    }

    @Test
    void testPartialUpdate_AuthorNotFound_ThrowsException() {
        when(authorRepository.findByAuthorNumber("A123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                authorService.partialUpdate("A123", mock(UpdateAuthorRequest.class), 1L));
    }

    // -------------------------------
    // findTopAuthorByLendings
    // -------------------------------
    @Test
    void testFindTopAuthorByLendings() {
        Pageable pageable = PageRequest.of(0, 5);
        List<AuthorLendingView> mockList = List.of(mock(AuthorLendingView.class));
        when(authorRepository.findTopAuthorByLendings(pageable))
                .thenReturn(new PageImpl<>(mockList));

        List<AuthorLendingView> result = authorService.findTopAuthorByLendings();

        assertEquals(mockList, result);
        verify(authorRepository).findTopAuthorByLendings(pageable);
    }

    // -------------------------------
    // findBooksByAuthorNumber
    // -------------------------------
    @Test
    void testFindBooksByAuthorNumber() {
        String authorNumber = "A123";
        Book book = mock(Book.class);
        List<Book> books = List.of(book);
        when(bookRepository.findBooksByAuthorNumber(authorNumber)).thenReturn(books);

        List<Book> result = authorService.findBooksByAuthorNumber(authorNumber);

        assertEquals(books, result);
        verify(bookRepository).findBooksByAuthorNumber(authorNumber);
    }

    // -------------------------------
    // findCoAuthorsByAuthorNumber
    // -------------------------------
    @Test
    void testFindCoAuthorsByAuthorNumber() {
        String authorNumber = "A123";
        Author author = mock(Author.class);
        List<Author> coAuthors = List.of(author);
        when(authorRepository.findCoAuthorsByAuthorNumber(authorNumber)).thenReturn(coAuthors);

        List<Author> result = authorService.findCoAuthorsByAuthorNumber(authorNumber);

        assertEquals(coAuthors, result);
        verify(authorRepository).findCoAuthorsByAuthorNumber(authorNumber);
    }

    // -------------------------------
    // removeAuthorPhoto
    // -------------------------------
    @Test
    void testRemoveAuthorPhoto_Success() {
        Author author = mock(Author.class);
        Photo photo = mock(Photo.class);
        when(authorRepository.findByAuthorNumber("A123")).thenReturn(Optional.of(author));
        when(author.getPhoto()).thenReturn(photo);
        when(photo.getPhotoFile()).thenReturn("photo.png");
        when(authorRepository.save(author)).thenReturn(author);

        Optional<Author> result = authorService.removeAuthorPhoto("A123", 1L);

        assertTrue(result.isPresent());
        verify(author).removePhoto(1L);
        verify(photoRepository).deleteByPhotoFile("photo.png");
    }

    @Test
    void testRemoveAuthorPhoto_NotFound_ThrowsException() {
        when(authorRepository.findByAuthorNumber("A123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                authorService.removeAuthorPhoto("A123", 1L));
    }
}
