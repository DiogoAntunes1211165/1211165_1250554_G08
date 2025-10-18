package pt.psoft.g1.psoftg1.bookmanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Book save(Book book) {

        BookEntity bookEntity = bookEntityMapper.toEntity(book);


        List<AuthorEntity> authors = new ArrayList<>(); // Lista para autores que serão associados ao livro

        for (AuthorEntity author : bookEntity.getAuthors()) {
            // Verifica se o autor já existe no banco de dados pelo nome
            AuthorEntity existingAuthor = authorRepository.searchByNameName(author.getName()).get(0);
            if (existingAuthor == null) {
                // Se o autor não existe, salva o novo autor
                existingAuthor = authorRepository.save(author);
            }
            authors.add(existingAuthor); // Adiciona o autor à lista de autores do livro
        }

        if (bookEntity.getGenre() != null) {
            // Verifica se o gênero já existe no banco de dados pelo nome
            GenreEntity existingGenre = genreRepository.findByString(bookEntity.getGenre().getGenre()).get();
            if (existingGenre == null) {
                // Se o gênero não existe, salva o novo gênero
                existingGenre = genreRepository.save(bookEntity.getGenre());
                bookEntity.setGenre(existingGenre); // Atualiza o gênero da BookEntity com o gênero persistido
            }
            bookEntity.setGenre(existingGenre); // Atualiza o gênero da BookEntity com o gênero persistido
        }

        // Atualiza a lista de autores da BookEntity com os autores persistidos
        bookEntity.setAuthors(authors);

        BookEntity savedEntity = bookRepositorySqlServer.save(bookEntity);

        return bookEntityMapper.toDomain(savedEntity);

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