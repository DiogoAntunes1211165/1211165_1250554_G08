package pt.psoft.g1.psoftg1.bookmanagement.repositories.relational;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;



public interface BookRepositorySQL extends CrudRepository<BookEntity,Integer> {

    @Query("SELECT b " +
            "FROM BookEntity b " +
            "WHERE b.isbn.isbn = :isbn")
    Optional<BookEntity> findByIsbn(@Param("isbn") String isbn);


    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO(b, COUNT(l)) " +
            "FROM BookEntity b " +
            "JOIN LendingEntity l ON l.book = b " +
            "WHERE l.startDate > :oneYearAgo " +
            "GROUP BY b " +
            "ORDER BY COUNT(l) DESC")
    Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable);


    @Query("SELECT b " +
            "FROM BookEntity b " +
            "WHERE b.genre.genre LIKE %:genre%")
    List<BookEntity> findByGenre(@Param("genre") String genre);


    @Query("SELECT b FROM BookEntity b WHERE b.title.title LIKE %:title%")
    List<BookEntity> findByTitle(@Param("title") String title);


    @Query(value =
            "SELECT b.* " +
                    "FROM Book b " +
                    "JOIN BOOK_AUTHORS on b.pk = BOOK_AUTHORS.BOOK_ENTITY_PK " +
                    "JOIN AUTHOR a on BOOK_AUTHORS.AUTHORS_AUTHOR_NUMBER = a.AUTHOR_NUMBER " +
                    "WHERE a.NAME LIKE %:authorName%"
            , nativeQuery = true)
    List<BookEntity> findByAuthorName(@Param("authorName") String authorName);


    @Query(value =
            "SELECT b.* " +
                    "FROM Book b " +
                    "JOIN BOOK_AUTHORS on b.pk = BOOK_AUTHORS.BOOK_ENTITY_PK " +
                    "JOIN AUTHOR a on BOOK_AUTHORS.AUTHORS_AUTHOR_NUMBER = a.AUTHOR_NUMBER " +
                    "WHERE a.AUTHOR_NUMBER = :authorNumber "
            , nativeQuery = true)
    List<BookEntity> findBooksByAuthorNumber(String authorNumber);




}
