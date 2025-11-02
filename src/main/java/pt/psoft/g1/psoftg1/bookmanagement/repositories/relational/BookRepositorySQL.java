package pt.psoft.g1.psoftg1.bookmanagement.repositories.relational;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookRepositorySQL extends CrudRepository<BookEntity, Long> {

    @Cacheable(value = "bookByIsbn", key = "#isbn")
    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN FETCH b.authors a WHERE b.isbn.isbn = :isbn")
    Optional<BookEntity> findByIsbn(@Param("isbn") String isbn);


    @Cacheable(value = "topBooksLent", key = "#oneYearAgo.toString().concat('-').concat(#pageable.pageNumber).concat('-').concat(#pageable.pageSize)")
    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO(b, COUNT(l)) " +
            "FROM BookEntity b " +
            "JOIN LendingEntity l ON l.book = b " +
            "WHERE l.startDate > :oneYearAgo " +
            "GROUP BY b " +
            "ORDER BY COUNT(l) DESC")
    Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable);

    // Fetch authors to avoid LazyInitializationException when mapping to domain outside transaction

    @Cacheable(value = "booksByGenre", key = "#genre")
    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN FETCH b.authors a WHERE b.genre.genre LIKE %:genre%")
    List<BookEntity> findByGenre(@Param("genre") String genre);

    @Cacheable(value = "booksByTitle", key = "#title")
    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN FETCH b.authors a WHERE b.title.title LIKE %:title%")
    List<BookEntity> findByTitle(@Param("title") String title);

    @Cacheable(value = "booksByAuthorName", key = "#authorName")
    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN FETCH b.authors a JOIN a.name an WHERE an.name LIKE %:authorName%")
    List<BookEntity> findByAuthorName(@Param("authorName") String authorName);

    @Cacheable(value = "booksByAuthorNumber", key = "#authorNumber")
    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN FETCH b.authors a WHERE a.authorNumber = :authorNumber")
    List<BookEntity> findBooksByAuthorNumber(@Param("authorNumber") String authorNumber);
}
