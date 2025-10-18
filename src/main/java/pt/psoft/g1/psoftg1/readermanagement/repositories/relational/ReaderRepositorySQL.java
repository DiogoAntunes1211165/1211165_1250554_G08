package pt.psoft.g1.psoftg1.readermanagement.repositories.relational;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReaderRepositorySQL extends CrudRepository<ReaderDetailsEntity, Long> {

    @Query(
            value = "SELECT * FROM reader_details rd WHERE rd.reader_number = :readerNumber",
            nativeQuery = true
    )
    Optional<ReaderDetailsEntity> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

    @Query(
            value = "SELECT * FROM reader_details rd WHERE rd.phone_number = :phoneNumber",
            nativeQuery = true
    )
    List<ReaderDetailsEntity> findByPhoneNumber(@Param("phoneNumber") @NotNull String phoneNumber);

    @Query(
            value = "SELECT rd.* FROM reader_details rd JOIN reader u ON u.pk = rd.reader_pk WHERE u.username = :username",
            nativeQuery = true
    )
    Optional<ReaderDetailsEntity> findByUsername(@Param("username") @NotNull String username);

    @Query(
            value = "SELECT rd.* FROM reader_details rd JOIN reader u ON u.pk = rd.reader_pk WHERE u.pk = :userId",
            nativeQuery = true
    )
    Optional<ReaderDetailsEntity> findByUserId(@Param("userId") @NotNull Long userId);

    @Query(
            value = "SELECT COUNT(rd.pk) FROM reader_details rd JOIN reader u ON u.pk = rd.reader_pk WHERE YEAR(u.created_at) = YEAR(GETDATE())",
            nativeQuery = true
    )
    int getCountFromCurrentYear();

    @Query(
            value = "SELECT rd.* FROM reader_details rd JOIN lending l ON l.reader_details_pk = rd.pk GROUP BY rd.pk ORDER BY COUNT(l.pk) DESC",
            nativeQuery = true
    )
   Page<ReaderDetailsEntity> findTopReaders(Pageable pageable);

    @Query(
            value = """
        SELECT rd.*, COUNT(l.pk) AS lending_count
        FROM reader_details rd
        JOIN lending l ON l.reader_details_pk = rd.pk
        JOIN book b ON b.pk = l.book_pk
        JOIN genre g ON g.pk = b.genre_pk
        WHERE g.genre = :genre
          AND l.start_date >= :startDate
          AND l.start_date <= :endDate
        GROUP BY rd.pk
        ORDER BY lending_count DESC
        """,
            nativeQuery = true
    )
    Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable,
                                            @Param("genre") String genre,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
