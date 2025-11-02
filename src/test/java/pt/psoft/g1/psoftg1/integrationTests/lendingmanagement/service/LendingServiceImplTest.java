package pt.psoft.g1.psoftg1.integrationTests.lendingmanagement.service;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedRequest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest
class LendingServiceImplTest {
    @Autowired
    private LendingService lendingService;
    @Autowired
    private LendingRepository lendingRepository;
    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;

    private Lending lending;
    private ReaderDetails readerDetails;
    private User user;
    private Reader reader;
    private boolean createdReaderDetails = false;
    private boolean createdReader = false;
    private Book book;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        author = new Author("Manuel Antonio Pina",
                "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                null,null);
        authorRepository.save(author);

        genre = new Genre("Género");
        genreRepository.save(genre);

        List<Author> authors = List.of(author);
        book = new Book("9782826012092",
                "O Inspetor Max",
                "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
                genre,
                authors,
                null);
        bookRepository.save(book);

        final String username = "manuel@gmail.com";
        // If a ReaderDetails already exists (bootstrapper may have created it), reuse it
        var maybeReaderDetails = readerRepository.findByUsername(username);
        if (maybeReaderDetails.isPresent()) {
            readerDetails = maybeReaderDetails.get();
            reader = readerDetails.getReader();
            user = reader;
            createdReaderDetails = false;
            createdReader = false;
        } else {
            // create new Reader and ReaderDetails
            reader = Reader.newReader(username, "Manuelino123!", "Manuel Sarapinto das Coives");
            userRepository.save(reader);
            user = reader;
            createdReader = true;

            readerDetails = new ReaderDetails(1,
                    reader,
                    "2000-01-01",
                    "919191919",
                    true,
                    true,
                    true,
                    null,null);
            readerRepository.save(readerDetails);
            createdReaderDetails = true;
        }

        // Create and save the lending
        lending = Lending.newBootstrappingLending(book,
                readerDetails,
                LocalDate.now().getYear(),
                999,
                LocalDate.of(LocalDate.now().getYear(), 1,1),
                LocalDate.of(LocalDate.now().getYear(), 1,11),
                15,
                300);
        lendingRepository.save(lending);

    }

    @AfterEach
    void tearDown() {
        //lendingRepository.delete(lending);
        if (createdReaderDetails) {
            readerRepository.delete(readerDetails);
        }
        if (createdReader) {
            userRepository.delete(user);
        }
        bookRepository.delete(book);
        genreRepository.delete(genre);
        authorRepository.delete(author);
    }

   /* @Test
    void testFindByLendingNumber() {
        assertThat(lendingService.findByLendingNumber(LocalDate.now().getYear() + "/999")).isPresent();
        assertThat(lendingService.findByLendingNumber(LocalDate.now().getYear() + "/1")).isEmpty();
    } */



    @Test
    void testSetReturned() {
        int year = 2024, seq = 888;
        var notReturnedLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                year,
                seq,
                LocalDate.of(2024, 3,1),
                null,
                15,
                300));
        var request = new SetLendingReturnedRequest(null);
        assertThrows(StaleObjectStateException.class,
                () -> lendingService.setReturned(year + "/" + seq, request, (notReturnedLending.getVersion()-1)));

    }

}