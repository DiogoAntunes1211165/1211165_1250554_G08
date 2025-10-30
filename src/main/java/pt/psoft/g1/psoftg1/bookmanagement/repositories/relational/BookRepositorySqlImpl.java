package pt.psoft.g1.psoftg1.bookmanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;
import  pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.authormanagement.repositories.relational.AuthorRepositorySQL;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers.BookEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;
import pt.psoft.g1.psoftg1.genremanagement.repositories.relational.GenreRepositorySql;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.PersistenceException;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;

@Profile("sqlServer")
@Qualifier("bookSqlServerRepo")
@Component
public class BookRepositorySqlImpl implements BookRepository {

    private final BookRepositorySQL bookRepositorySqlServer;
    private final BookEntityMapper bookEntityMapper;

    private final GenreRepositorySql genreRepository;
    private final AuthorRepositorySQL authorRepository;
    private final EntityManager em;


    @Autowired
    @Lazy
    public BookRepositorySqlImpl(BookRepositorySQL bookRepositorySqlServer, BookEntityMapper bookEntityMapper, GenreRepositorySql genreRepository, AuthorRepositorySQL authorRepository, EntityManager em) {
        this.bookRepositorySqlServer = bookRepositorySqlServer;
        this.bookEntityMapper = bookEntityMapper;
        this.genreRepository = genreRepository;
        this.authorRepository = authorRepository;
        this.em = em;
    }


    @Override
    public List<Book> findByGenre(String genre) {
        List<Book> book = new ArrayList<>();
        for (BookEntity b: bookRepositorySqlServer.findByGenre(genre)) {
            book.add(bookEntityMapper.toDomain(b));
        }
        return book;
    }

    @Override
    public List<Book> findByTitle(String title) {
        List<Book> book = new ArrayList<>();
        for (BookEntity b: bookRepositorySqlServer.findByTitle(title)) {
            book.add(bookEntityMapper.toDomain(b));
        }
        return book;
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        List<Book> book = new ArrayList<>();
        List<BookEntity> books=bookRepositorySqlServer.findByAuthorName(authorName);
        //System.out.println(book);
        for (BookEntity b: books) {
            book.add(bookEntityMapper.toDomain(b));
        }
        return book;
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        if (bookRepositorySqlServer.findByIsbn(isbn).isEmpty()) {
            return Optional.empty();
        }else{
            BookEntity book = bookRepositorySqlServer.findByIsbn(isbn).get();

            return Optional.of(bookEntityMapper.toDomain(book)) ;
        }

    }

    @Override
    public Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        return bookRepositorySqlServer.findTop5BooksLent(oneYearAgo, pageable);
    }

    @Override
    public List<Book> findBooksByAuthorNumber(String authorNumber) {
        List<Book> books = new ArrayList<>();
        // Put the string authorNumber to long
        for (BookEntity bookEntity : bookRepositorySqlServer.findBooksByAuthorNumber(String.valueOf(Long.parseLong(authorNumber)))) {
            books.add(bookEntityMapper.toDomain(bookEntity));
        }
        return books;

    }


    @Override
    @CacheEvict(value = "bookByIsbn", key = "#book.isbn")
    public Book save(Book book) {

        // Map domain to a temporary entity
        BookEntity newEntity = bookEntityMapper.toEntity(book);

        // Resolve authors: ensure each AuthorEntity is the persisted instance
        List<AuthorEntity> resolvedAuthors = new ArrayList<>();
        for (AuthorEntity author : newEntity.getAuthors()) {
            List<AuthorEntity> found = authorRepository.searchByNameName(author.getName());
            AuthorEntity existingAuthor = null;
            if (found != null && !found.isEmpty()) existingAuthor = found.get(0);
            if (existingAuthor == null) existingAuthor = authorRepository.save(author);
            resolvedAuthors.add(existingAuthor);
        }
        newEntity.setAuthors(resolvedAuthors);

        // Resolve genre
        if (newEntity.getGenre() != null) {
            Optional<GenreEntity> existingGenreOpt = genreRepository.findByString(newEntity.getGenre().getGenre());
            if (existingGenreOpt.isPresent()) {
                newEntity.setGenre(existingGenreOpt.get());
            } else {
                GenreEntity saved = genreRepository.save(newEntity.getGenre());
                newEntity.setGenre(saved);
            }
        }

        final String isbn = book.getIsbn();

        int attempts = 0;
        while (true) {
            try {
                // Try to find existing BookEntity by ISBN using EntityManager (bypass repository cache)
                BookEntity existing;
                try {
                    TypedQuery<BookEntity> tq = em.createQuery("SELECT DISTINCT b FROM BookEntity b WHERE b.isbn.isbn = :isbn", BookEntity.class);
                    tq.setParameter("isbn", isbn);
                    existing = tq.getSingleResult();
                } catch (NoResultException nre) {
                    existing = null;
                }

                if (existing != null) {
                    // Update mutable fields on existing entity
                    existing.setAuthors(newEntity.getAuthors());
                    existing.setGenre(newEntity.getGenre());
                    // Copy embedded title and description via reflection (fields are private)
                    try {
                        Field titleField = BookEntity.class.getDeclaredField("title");
                        titleField.setAccessible(true);
                        titleField.set(existing, BookEntity.class.getDeclaredField("title").get(newEntity));

                        Field descField = BookEntity.class.getDeclaredField("description");
                        descField.setAccessible(true);
                        descField.set(existing, BookEntity.class.getDeclaredField("description").get(newEntity));
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    }

                    BookEntity saved = bookRepositorySqlServer.save(existing);
                    return bookEntityMapper.toDomain(saved);
                } else {
                    // No existing book -> insert new
                    BookEntity saved = bookRepositorySqlServer.save(newEntity);
                    return bookEntityMapper.toDomain(saved);
                }
            } catch (DataIntegrityViolationException | PersistenceException ex) {
                attempts++;
                if (attempts > 1) {
                    throw new ConflictException("Could not update book: " + ex.getMessage());
                }
                // Try to recover: another transaction may have inserted the book concurrently. Re-query and loop to update it.
                try {
                    TypedQuery<BookEntity> tq = em.createQuery("SELECT b FROM BookEntity b WHERE b.isbn.isbn = :isbn", BookEntity.class);
                    tq.setParameter("isbn", isbn);
                    BookEntity found = tq.getSingleResult();
                    // copy fields into newEntity to allow update path on retry
                    try {
                        Field pkField = BookEntity.class.getDeclaredField("pk");
                        pkField.setAccessible(true);
                        pkField.setLong(newEntity, found.getPk());

                        Field versionField = BookEntity.class.getDeclaredField("version");
                        versionField.setAccessible(true);
                        versionField.set(newEntity, found.getVersion());
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    }
                    // loop will retry and hit existing path
                } catch (PersistenceException ex2) {
                    throw new ConflictException("Could not update book: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void delete(Book book) {

    }

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query)
    {
        String title = query.getTitle();
        String genre = query.getGenre();
        String authorName = query.getAuthorName();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<BookEntity> cq = cb.createQuery(BookEntity.class);
        final Root<BookEntity> root = cq.from(BookEntity.class);
        final Join<BookEntity, Genre> genreJoin = root.join("genre");
        final Join<BookEntity, Author> authorJoin = root.join("authors");
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(title))
            where.add(cb.like(root.get("title").get("title"), title + "%"));

        if (StringUtils.hasText(genre))
            where.add(cb.like(genreJoin.get("genre"), genre + "%"));

        if (StringUtils.hasText(authorName))
            where.add(cb.like(authorJoin.get("name").get("name"), authorName + "%"));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("title"))); // Order by title, alphabetically

        final TypedQuery<BookEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List <Book> books = new ArrayList<>();

        for (BookEntity bookEntity : q.getResultList()) {
            books.add(bookEntityMapper.toDomain(bookEntity));
        }

        return books;
    }

}
