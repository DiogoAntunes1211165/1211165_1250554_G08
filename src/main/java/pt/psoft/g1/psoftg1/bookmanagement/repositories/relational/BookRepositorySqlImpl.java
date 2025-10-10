package pt.psoft.g1.psoftg1.bookmanagement.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers.BookEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Profile("sqlServer")
@Qualifier("BookRepositorySqlImpl")
@Component
public class BookRepositorySqlImpl implements BookRepository {

    private BookEntityMapper bookEntityMapper;

    private BookRepositorySQL bookRepositorySQL;


    @Autowired
    @Lazy
    private BookRepositorySqlImpl(BookRepositorySQL bookRepository, BookEntityMapper bookEntityMapper) {
        this.bookEntityMapper = bookEntityMapper;
        this.bookRepositorySQL = bookRepository;
    }


    @Override
    public List<Book> findByGenre(String genre) {
        List<Book> bookEntities = new ArrayList<>();
        bookRepositorySQL.findByGenre(genre).forEach(bookEntity -> bookEntities.add(bookEntityMapper.toDomain(bookEntity))); // Convert each BookEntity to Book and add to the list
        return bookEntities;
    }

    @Override
    public List<Book> findByTitle(String title) {
        List<Book> bookEntities = new ArrayList<>();
        bookRepositorySQL.findByTitle(title).forEach(bookEntity -> bookEntities.add(bookEntityMapper.toDomain(bookEntity))); // Convert each BookEntity to Book and add to the list
        return bookEntities;
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        List<Book> bookEntities = new ArrayList<>();
        bookRepositorySQL.findByAuthorName(authorName).forEach(bookEntity -> bookEntities.add(bookEntityMapper.toDomain(bookEntity)));
        return bookEntities;
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        if (bookRepositorySQL.findByIsbn(isbn).isEmpty()) {
            return Optional.empty();
        } else {
            Book book = bookEntityMapper.toDomain(bookRepositorySQL.findByIsbn(isbn).get()); // Convert BookEntity to Book
            return Optional.of(book);
        }
    }

    @Override
    public Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        return null;
    }

    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber) {
        List<Book> bookEntities = new ArrayList<>();
        bookRepositorySQL.findBooksByAuthorNumber(authorNumber).forEach(bookEntity -> bookEntities.add(bookEntityMapper.toDomain(bookEntity)));
        return bookEntities;
    }

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query) {
        List<Book> bookEntities = new ArrayList<>();
        bookRepositorySQL.findAll().forEach(bookEntity -> bookEntities.add(bookEntityMapper.toDomain(bookEntity)));
        return bookEntities;
    }

    @Override
    public Book save(Book book) {
        return bookEntityMapper.toDomain(bookRepositorySQL.save(bookEntityMapper.toEntity(book))); // Convert Book to BookEntity, save it, and convert back to Book
    }

    @Override
    public void delete(Book book) {
        bookRepositorySQL.delete(bookEntityMapper.toEntity(book));// Convert Book to BookEntity and delete it
    }
}
