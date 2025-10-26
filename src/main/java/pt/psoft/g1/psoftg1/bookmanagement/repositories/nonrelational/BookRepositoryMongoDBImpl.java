package pt.psoft.g1.psoftg1.bookmanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.AuthorDocument;
import pt.psoft.g1.psoftg1.authormanagement.repositories.nonrelational.AuthorMongoDBPersistence;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.BookDocument;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers.BookDocumentMapper;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;
import pt.psoft.g1.psoftg1.genremanagement.repositories.nonrelational.GenreDocumentPersistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("BookRepositoryMongoDBImpl")
public class BookRepositoryMongoDBImpl implements BookRepository {

    private final BookDocumentPersistence bookDocumentPersistence;
    private final BookDocumentMapper bookDocumentMapper;
    private final GenreDocumentPersistence genreDocumentPersistence;
    private final AuthorMongoDBPersistence authorDocumentPersistence;

    @Autowired
    @Lazy
    public BookRepositoryMongoDBImpl(
            BookDocumentPersistence bookDocumentPersistence,
            BookDocumentMapper bookDocumentMapper,
            GenreDocumentPersistence genreDocumentPersistence,
            AuthorMongoDBPersistence authorDocumentPersistence
    ) {
        this.bookDocumentPersistence = bookDocumentPersistence;
        this.bookDocumentMapper = bookDocumentMapper;
        this.genreDocumentPersistence = genreDocumentPersistence;
        this.authorDocumentPersistence = authorDocumentPersistence;
    }

    // ------------------------------------------------------
    // MÉTODOS
    // ------------------------------------------------------

    @Override
    public List<Book> findByGenre(String genre) {
        List<BookDocument> docs = bookDocumentPersistence.findByGenre_Genre(genre);
        List<Book> books = new ArrayList<>();
        for (BookDocument d : docs) {
            books.add(bookDocumentMapper.toDomain(d));
        }
        return books;
    }

    @Override
    public List<Book> findByTitle(String title) {
        List<BookDocument> docs = bookDocumentPersistence.findByTitleContainingIgnoreCase(title);
        List<Book> books = new ArrayList<>();
        for (BookDocument d : docs) {
            books.add(bookDocumentMapper.toDomain(d));
        }
        return books;
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        List<BookDocument> docs = bookDocumentPersistence.findByAuthors_NameContainingIgnoreCase(authorName);
        List<Book> books = new ArrayList<>();
        for (BookDocument d : docs) {
            books.add(bookDocumentMapper.toDomain(d));
        }
        return books;
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        Optional<BookDocument> doc = bookDocumentPersistence.findByIsbn(isbn);
        return doc.map(bookDocumentMapper::toDomain);
    }

    @Override
    public Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        // MongoDB não tem query agregada equivalente pronta.
        // Implementa depois com aggregation pipeline se precisares.
        return Page.empty();
    }

    @Override
    public List<Book> findBooksByAuthorNumber(String authorNumber) {
        List<BookDocument> docs = bookDocumentPersistence.findByAuthors_AuthorNumber(authorNumber);
        List<Book> books = new ArrayList<>();
        for (BookDocument d : docs) {
            books.add(bookDocumentMapper.toDomain(d));
        }
        return books;
    }

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query) {
        List<BookDocument> results = bookDocumentPersistence.searchBooks(query.getTitle(), query.getGenre(), query.getAuthorName());
        List<Book> books = new ArrayList<>();
        for (BookDocument d : results) {
            books.add(bookDocumentMapper.toDomain(d));
        }
        return books;
    }

    @Override
    public Book save(Book book) {
        BookDocument doc = bookDocumentMapper.toDocument(book);

        // Tratar autores
        List<AuthorDocument> savedAuthors = new ArrayList<>();
        for (AuthorDocument authorDoc : doc.getAuthors()) {
            List<AuthorDocument> existingAuthors = authorDocumentPersistence.findByName(authorDoc.getName().toString());

            AuthorDocument persisted;
            if (existingAuthors == null || existingAuthors.isEmpty()) {
                persisted = authorDocumentPersistence.save(authorDoc);
            } else {
                persisted = existingAuthors.get(0); // usa o primeiro encontrado
            }
            savedAuthors.add(persisted);
        }
        doc.setAuthors(savedAuthors);

        // Tratar género
        if (doc.getGenre() != null) {
            GenreDocument genreDoc = genreDocumentPersistence
                    .findByString(doc.getGenre().getGenre())
                    .orElseGet(() -> genreDocumentPersistence.save(doc.getGenre()));
            doc.setGenre(genreDoc);
        }

        BookDocument savedDoc = bookDocumentPersistence.save(doc);
        return bookDocumentMapper.toDomain(savedDoc);
    }

    @Override
    public void delete(Book book) {
        Optional<BookDocument> doc = bookDocumentPersistence.findByIsbn(book.getIsbn());
        doc.ifPresent(bookDocumentPersistence::delete);
    }
}
