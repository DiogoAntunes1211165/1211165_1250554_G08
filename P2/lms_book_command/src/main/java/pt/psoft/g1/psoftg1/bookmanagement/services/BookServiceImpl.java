package pt.psoft.g1.psoftg1.bookmanagement.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.publishers.AuthorEventsPublisher;
import pt.psoft.g1.psoftg1.authormanagement.publishers.AuthorEventsPublisherImpl;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.*;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisher;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisherImpl;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource({"classpath:config/library.properties"})
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;
    private final PhotoRepository photoRepository;


    // Substitui a injeção única por uma lista para suportar múltiplos providers (Google, OpenLibrary, ...)
    private final List<IsbnLookupService> isbnLookupServices;
    private final BookEventsPublisher bookEventsPublisher;
    private final AuthorEventsPublisher authorEventsPublisher;

    @Value("${suggestionsLimitPerGenre}")
    private long suggestionsLimitPerGenre;




    //
    @Override
    public Book create(BookViewAMQP bookViewAMQP) {
        final String isbn = bookViewAMQP.getIsbn();

        // Primeiro tenta buscar o livro existente pelo ISBN
        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        if (existingBook.isPresent()) {
            // Livro já existe → apenas retorna
            return existingBook.get();
        }

        // Livro não existe → cria um novo
        final String title = bookViewAMQP.getTitle();
        final String photoURI = null;
        final String description = bookViewAMQP.getDescription();
        final String genre = bookViewAMQP.getGenre();
        final List<String> authorsIds = bookViewAMQP.getAuthorsIDs();

        Book createdBook = create(isbn, title, photoURI, description, genre, authorsIds);

        return createdBook;
    }


    // Método auxiliar para criar um livro a partir dos parâmetros fornecidos.
    private Book create(String isbn, String title, String photoURI, String description, String genre, List<String> authorsIds) {
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            throw new ConflictException("Book with ISBN " + isbn + " already exists");
        }

        List<Author> authors = new ArrayList<>();
        if (authorsIds != null) {
            for (String authorID : authorsIds) {
                if (authorID == null || authorID.isBlank()) continue;
                authorRepository.findByAuthorNumber(authorID).ifPresent(authors::add);
            }
        }

        Genre genreObj = genreRepository.findByString(genre)
                .orElseThrow(() -> new NotFoundException("Genre not found"));

        Book newBook = new Book(isbn, title, description, genreObj, authors, photoURI);

        return bookRepository.save(newBook);
    }

    // Cria um livro a partir de uma requisição de criação.
    @Override
    public Book create(CreateBookRequest request, String isbn) {

        final String title = request.getTitle();
        final String description = request.getDescription();
        final String photoURI = request.getPhotoURI();
        final String genre = request.getGenre();
        final List<String> authorIds = request.getAuthors();

        Book savedBook = create(isbn, title, description, photoURI, genre, authorIds);

        if(savedBook!=null) {
            bookEventsPublisher.sendBookCreated(savedBook);
        }

        return savedBook;
    }

// Cria um livro, gênero e autor a partir de uma requisição de criação completa.
    @Override
    public Book create(CreateFullProcessRequest request, String isbn) {
        // Creating the genre for the new book
        System.out.println("Creating book with full process");
        System.out.println("Creating the Genre first");
        Genre genreCreated = createGenre(request.getGenre());
        System.out.println("Genre created");
        System.out.println("Genre with the following properties: " + genreCreated.getGenre());
        // Creating the author now
        System.out.println("Creating the author now");
        Author authorCreated = createAuthor(request.getName(), request.getBio(), request.getAuthorPhotoURI());
        System.out.println("Author created");
        System.out.println("Author with the following properties: " + authorCreated.getAuthorNumber() + " " +
                authorCreated.getName() + " " + authorCreated.getBio());

        // Creating the book now
        System.out.println("Creating the book now");
        Book bookCreated = create(isbn, request.getTitle(), request.getDescription(), request.getBookPhotoURI(),
                genreCreated.getGenre(), List.of(authorCreated.getAuthorNumber()));

        if(bookCreated != null) {
            bookEventsPublisher.sendBookCreated(bookCreated);
        }

        return bookCreated;
    }

    private Author createAuthor(String name, String bio, String photoURI){

        Author author = new Author(name, bio, photoURI, null);

        Author savedAuthor = authorRepository.save(author);
        if (savedAuthor != null) {
            authorEventsPublisher.sendAuthorCreated(savedAuthor);
        }
        return savedAuthor;
    }

    private Genre createGenre(@NotBlank String genre) {
        return null;
    }


    // Método auxiliar que tenta todos os IsbnLookupService em ordem até obter um ISBN.
    private Optional<String> findIsbnFromProviders(String title) {
        if (title == null || title.isBlank()) return Optional.empty();
        if (isbnLookupServices == null || isbnLookupServices.isEmpty()) {
            logger.warn("No IsbnLookupService beans available to query for title: {}", title);
            return Optional.empty();
        }

        for (IsbnLookupService svc : isbnLookupServices) {
            try {
                Optional<String> res = svc.findIsbnByTitle(title);
                if (res.isPresent()) {
                    logger.info("BookService: found ISBN '{}' via provider '{}' for title '{}'",
                            res.get(), svc.getServiceName(), title);
                    return res;
                } else {
                    logger.debug("BookService: provider '{}' returned no result for title '{}'",
                            svc.getServiceName(), title);
                }
            } catch (Exception e) {
                logger.warn("BookService: provider '{}' failed for title '{}': {}",
                        svc.getServiceName(), title, e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public Book update(UpdateBookRequest request, String currentVersion) {

        var book = findByIsbn(request.getIsbn());

        List<String> authorsId = request.getAuthors();

        MultipartFile photo = request.getPhoto();
        String photoURI = request.getPhotoURI();
        if (photo == null && photoURI != null || photo != null && photoURI == null) {
            photoURI = null;
        }

        String genreId = request.getGenre();
        String title = request.getTitle();
        String description = request.getDescription();

        Book updatedBook = update( book, currentVersion, title, description, photoURI, genreId, authorsId);
        if( updatedBook!=null ) {
            bookEventsPublisher.sendBookUpdated(updatedBook, currentVersion);
        }

        return updatedBook;
    }

    @Override
    public Book update(BookViewAMQP bookViewAMQP) {
            final String version = bookViewAMQP.getVersion();
            final String isbn = bookViewAMQP.getIsbn();
            final String description = bookViewAMQP.getDescription();
            final String title = bookViewAMQP.getTitle();
            final String photoURI = null;
            final String genre = bookViewAMQP.getGenre();
            final List<String> authorIds = bookViewAMQP.getAuthorsIDs();

            var book = findByIsbn(isbn);

            Book bookUpdated = update(book, version, title, description, photoURI, genre, authorIds);

            return bookUpdated;
        }


    // Método auxiliar para atualizar um livro a partir dos parâmetros fornecidos.
    private Book update(Book book, String currentVersion, String title, String photoURI, String description, String genreId, List<String> authorsId) {

        Genre genreObj = null;
        if (genreId != null) {
            Optional<Genre> genre = genreRepository.findByString(genreId);
            if (genre.isEmpty()) {
                throw new NotFoundException("Genre not found");
            }
            genreObj = genre.get();
        }

        List<Author> authors = new ArrayList<>();
        if (authorsId != null) {
            for (String authorNumber : authorsId) {
                Optional<Author> temp = authorRepository.findByAuthorNumber(authorNumber);
                if (temp.isEmpty()) {
                    continue;
                }
                Author author = temp.get();
                authors.add(author);
            }
        }
        else
            authors = null;

        book.applyPatch(currentVersion, title, description, photoURI, genreObj, authors);

        Book updatedBook = bookRepository.save(book);

        return updatedBook;
    }




    @Override
    public Book save(Book book) {
        return this.bookRepository.save(book);
    }

    @Override
    public List<BookCountDTO> findTop5BooksLent(){
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        Pageable pageableRules = PageRequest.of(0,5);
        return this.bookRepository.findTop5BooksLent(oneYearAgo, pageableRules).getContent();
    }

    @Override
    public Book removeBookPhoto(String isbn, String desiredVersion) {
        Book book = this.findByIsbn(isbn);
        String photoFile;
        try {
            photoFile = book.getPhoto().getPhotoFile();
        }catch (NullPointerException e){
            throw new NotFoundException("Book did not have a photo assigned to it.");
        }

        book.removePhoto(desiredVersion);
        var updatedBook = bookRepository.save(book);
        photoRepository.deleteByPhotoFile(photoFile);
        return updatedBook;
    }

    @Override
    public List<Book> findByGenre(String genre) {
        return this.bookRepository.findByGenre(genre);
    }

    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorName(authorName + "%");
    }

    public Book findByIsbn(String isbn) {
        return this.bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(Book.class, isbn));
    }



    @Override
    public List<Book> searchBooks(Page page, SearchBooksQuery query) {
        if (page == null) {
            page = new Page(1, 10);
        }
        if (query == null) {
            query = new SearchBooksQuery("", "", "");
        }
        return bookRepository.searchBooks(page, query);
    }
}