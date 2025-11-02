package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LendingRepositorySQL extends CrudRepository<LendingEntity, Long> {

    @Cacheable(value = "lendingByNumber", key = "#p0")
    @Query("SELECT DISTINCT l " +
            "FROM LendingEntity l " +
            // fetch the associated book and its authors to avoid lazy initialization outside the session
            "JOIN FETCH l.book b " +
            "LEFT JOIN FETCH b.authors " +
            "WHERE l.lendingNumberEntity.lendingNumber = :lendingNumber")
    Optional<LendingEntity> findByLendingNumber(String lendingNumber);


    @Cacheable(value = "lendingsByReaderAndIsbn", key = "#p0 + '-' + #p1")
    @Query("SELECT DISTINCT l " +
            "FROM LendingEntity l " +
            // fetch book and authors, and reader details to ensure needed associations are initialized
            "JOIN FETCH l.book b " +
            "LEFT JOIN FETCH b.authors " +
            "JOIN FETCH l.readerDetails r " +
            "WHERE b.isbn.isbn = :isbn " +
            "AND r.readerNumber.readerNumber = :readerNumber ")
    List<LendingEntity> listByReaderNumberAndIsbn(String readerNumber, String isbn);


    @Cacheable(value = "lendingCountCurrentYear")
    @Query("SELECT COUNT(l) " +
            "FROM LendingEntity l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();


    @Cacheable(value = "outstandingLendingsByReader", key = "#readerNumber")
    @Query("SELECT DISTINCT l " +
            "FROM LendingEntity l " +
            // fetch reader details and book+authors so the mapper can access them safely
            "JOIN FETCH l.readerDetails r " +
            "JOIN FETCH l.book b " +
            "LEFT JOIN FETCH b.authors " +
            "WHERE r.readerNumber.readerNumber = :readerNumber " +
            "AND l.returnedDate IS NULL")
    List<LendingEntity> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);


    @Cacheable(value = "averageLendingDuration")
    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM lending_entity l",
            nativeQuery = true)
    Double getAverageDuration();


    @Cacheable(value = "avgLendingDurationByIsbn", key = "#isbn")
    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM lending_entity l " +
                    "JOIN book b ON l.book_pk = b.pk " +
                    "WHERE b.isbn = :isbn",
            nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}

