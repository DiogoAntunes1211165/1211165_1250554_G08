package pt.psoft.g1.psoftg1.unitTests.bookmanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookController;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookView;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para o BookController.
 * Utiliza @WebMvcTest para testar apenas a camada de controller,
 * sem carregar todo o contexto da aplicação Spring Boot.
 */
@WebMvcTest(BookController.class)
class BookControllerTest {

    // MockMvc permite simular requisições HTTP sem iniciar um servidor real
    @Autowired
    private MockMvc mockMvc;

    // @MockBean cria mocks das dependências do controller injetadas pelo Spring
    // Estes mocks substituem os beans reais no contexto de teste
    @MockBean private BookService bookService;
    @MockBean private LendingService lendingService;
    @MockBean private ConcurrencyService concurrencyService;
    @MockBean private FileStorageService fileStorageService;
    @MockBean private UserService userService;
    @MockBean private ReaderService readerService;
    @MockBean private BookViewMapper bookViewMapper;


    /**
     * Testa a criação de um livro com sucesso.
     * Verifica se o endpoint PUT /api/books/{isbn} retorna 201 Created
     * e se o livro criado possui os dados corretos.
     */
    @Test
    @WithMockUser(username = "user", roles = {"USER"}) // Simula um utilizador autenticado
    void testCreateBook_Success() throws Exception {
        // Arrange - Preparação dos dados de teste

        // Mock do request de criação
        CreateBookRequest createBookRequest = mock(CreateBookRequest.class);

        String isbn = "12345";

        // Mock do objeto Book retornado pelo serviço
        Book book = mock(Book.class);

        // Mock da view que será retornada ao cliente
        BookView bookView = mock(BookView.class);
        Mockito.when(bookView.getIsbn()).thenReturn("12345");
        Mockito.when(bookView.getTitle()).thenReturn("Clean Code");


        // Configura o comportamento esperado: quando o serviço criar um livro, retorna o mock
        Mockito.when(bookService.create(any(CreateBookRequest.class), eq(isbn))).thenReturn(book);
        // Configura o mapper para converter Book em BookView
        Mockito.when(bookViewMapper.toBookView(book)).thenReturn(bookView);

        // JSON do corpo da requisição
        String requestBody = "{ \"isbn\": \"12345\", \"title\": \"Clean Code\", \"authorIds\": [1, 2], \"genreIds\": [1, 2], \"publisherId\": 1, \"publicationYear\": 2008 }";

        // Act & Assert - Executa a requisição e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/{isbn}", isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // Token CSRF para segurança
                .andExpect(status().isCreated()) // Verifica status HTTP 201
                .andExpect(jsonPath("$.isbn").value("12345")) // Verifica campo ISBN na resposta
                .andExpect(jsonPath("$.title").value("Clean Code")); // Verifica campo title na resposta
    }


    /**
     * Testa a busca de um livro por ISBN com sucesso.
     * Verifica se o endpoint GET /api/books/{isbn} retorna 200 OK
     * quando o livro existe na base de dados.
     */
    @Test
    @DisplayName("GET /api/books/{isbn} - Deve retornar um livro com sucesso")
    @WithMockUser(username = "user", roles = {"USER"}) // Simula utilizador autenticado
    void testFindByIsbn_Success() throws Exception {

        // Arrange - Preparação dos dados de teste

        // Mock do objeto Book retornado pelo repositório
        Book book = mock(Book.class);

        // Mock da view que será retornada ao cliente
        BookView bookView = mock(BookView.class);



        // Configura o comportamento: quando buscar pelo ISBN, retorna o mock do Book
        Mockito.when(bookService.findByIsbn("12345")).thenReturn(book);
        // Configura o mapper para converter Book em BookView
        Mockito.when(bookViewMapper.toBookView(book)).thenReturn(bookView);

        // Act & Assert - Executa a requisição e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/12345")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // Token CSRF
                        .andExpect(status().isOk()); // Verifica status HTTP 200

    }


    /**
     * Testa a busca de livros filtrando por título.
     * Verifica se o endpoint GET /api/books?title= retorna uma lista de livros
     * que correspondem ao título pesquisado.
     */
    @Test
    @DisplayName("GET /api/books?title= - Deve retornar lista de livros filtrada por título")
    @WithMockUser(username = "user", roles = {"USER"}) // Simula utilizador autenticado
    void testFindBooksByTitle() throws Exception {
        // Arrange - Preparação dos dados de teste

        // Mock do objeto Book retornado pelo serviço
        Book book = mock(Book.class);

        // Mock do BookView que será incluído na resposta
        BookView view = mock(BookView.class);
        Mockito.when(view.getIsbn()).thenReturn("111");
        Mockito.when(view.getTitle()).thenReturn("Domain Driven Design");


        // Configura o comportamento do serviço: busca por título retorna lista com 1 livro
        Mockito.when(bookService.findByTitle("Domain Driven Design"))
                .thenReturn(List.of(book));
        // Configura o mapper para converter lista de Books em lista de BookViews
        Mockito.when(bookViewMapper.toBookView(List.of(book)))
                .thenReturn(List.of(view)); // Retorna a lista com a view mockada

        // Act & Assert - Executa a requisição GET com query parameter e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .param("title", "Domain Driven Design") // Query parameter ?title=...
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()) // Verifica status HTTP 200
                .andExpect(jsonPath("$.items[0].isbn").value("111")) // Verifica ISBN do primeiro item
                .andExpect(jsonPath("$.items[0].title").value("Domain Driven Design")); // Verifica título do primeiro item
    }

}
