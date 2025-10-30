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

    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "WHERE l.lendingNumberEntity.lendingNumber = :lendingNumber")
    Optional<LendingEntity> findByLendingNumber(String lendingNumber);


    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "JOIN BookEntity b ON l.book.pk = b.pk " +
            "JOIN ReaderDetailsEntity r ON l.readerDetails.pk = r.pk " +
            "WHERE b.isbn.isbn = :isbn " +
            "AND r.readerNumber.readerNumber = :readerNumber ")
    List<LendingEntity> listByReaderNumberAndIsbn(String readerNumber, String isbn);


    @Query("SELECT COUNT(l) " +
            "FROM LendingEntity l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();


    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "JOIN ReaderDetailsEntity r ON l.readerDetails.pk = r.pk " +
            "WHERE r.readerNumber.readerNumber = :readerNumber " +
            "AND l.returnedDate IS NULL")
    List<LendingEntity> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);


    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM lending_entity l",
            nativeQuery = true)
    Double getAverageDuration();


    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM lending_entity l " +
                    "JOIN book b ON l.book_pk = b.pk " +
                    "WHERE b.isbn = :isbn",
            nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}
