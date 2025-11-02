package pt.psoft.g1.psoftg1.readermanagement.repositories.relational;

import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
// Use a distinct cache name for repository-layer entity caching to avoid clashes with
// service-layer caches that hold domain objects.
@CacheConfig(cacheNames = {"readerEntities"})
public interface ReaderRepositorySQL extends CrudRepository<ReaderDetailsEntity, Long> {

    // ---- SAVE ----
    // (Se existirem operações de escrita, limpam o cache)
    @Override
    @CacheEvict(allEntries = true)
    <S extends ReaderDetailsEntity> S save(S entity);

    @Override

    @CacheEvict(allEntries = true)
    <S extends ReaderDetailsEntity> Iterable<S> saveAll(Iterable<S> entities);


    @Override
    @Cacheable(key = "#id")
    Optional<ReaderDetailsEntity> findById(Long id);

    // ---- FIND BY READER NUMBER ----
    // Use JPQL with JOIN FETCH to eagerly load reader, its authorities and interestList to avoid LazyInitializationException
    @Query("SELECT DISTINCT r FROM ReaderDetailsEntity r " +
            "JOIN FETCH r.reader u " +
            "LEFT JOIN FETCH u.authorities " +
            "LEFT JOIN FETCH r.interestList " +
            "WHERE r.readerNumber.readerNumber = :readerNumber")
    @Cacheable(key = "#readerNumber")
    Optional<ReaderDetailsEntity> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

    // ---- FIND BY PHONE NUMBER ----
    @Query("SELECT DISTINCT r FROM ReaderDetailsEntity r " +
            "JOIN FETCH r.reader u " +
            "LEFT JOIN FETCH u.authorities " +
            "LEFT JOIN FETCH r.interestList " +
            "WHERE r.phoneNumber.phoneNumber = :phoneNumber")
    @Cacheable(key = "#phoneNumber")
    List<ReaderDetailsEntity> findByPhoneNumber(@Param("phoneNumber") @NotNull String phoneNumber);

    // ---- FIND BY USERNAME ----
    @Query("SELECT DISTINCT r FROM ReaderDetailsEntity r " +
            "JOIN FETCH r.reader u " +
            "LEFT JOIN FETCH u.authorities " +
            "LEFT JOIN FETCH r.interestList " +
            "WHERE u.username = :username")
    @Cacheable(key = "#username")
    Optional<ReaderDetailsEntity> findByUsername(@Param("username") @NotNull String username);

    // ---- FIND BY USER ID ----
    @Query("SELECT DISTINCT r FROM ReaderDetailsEntity r " +
            "JOIN FETCH r.reader u " +
            "LEFT JOIN FETCH u.authorities " +
            "LEFT JOIN FETCH r.interestList " +
            "WHERE u.id = :userId")
    @Cacheable(key = "#userId")
    Optional<ReaderDetailsEntity> findByUserId(@Param("userId") @NotNull Long userId);

    // ---- COUNT READERS FROM CURRENT YEAR ----
    @Query("SELECT COUNT(rd) " +
            "FROM ReaderDetailsEntity rd " +
            "JOIN UserEntity u ON rd.reader.id = u.id " +
            "WHERE YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    @Cacheable(key = "'countCurrentYear'")
    int getCountFromCurrentYear();

    // ---- TOP READERS ----
    @Query("SELECT rd " +
            "FROM ReaderDetailsEntity rd " +
            "JOIN LendingEntity l ON l.readerDetails.pk = rd.pk " +
            "GROUP BY rd " +
            "ORDER BY COUNT(l) DESC")
    @Cacheable(key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    Page<ReaderDetailsEntity> findTopReaders(Pageable pageable);

    // ---- TOP READERS BY GENRE ----
    @Query("SELECT NEW pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO(rd, count(l)) " +
            "FROM ReaderDetailsEntity rd " +
            "JOIN LendingEntity l ON l.readerDetails.pk = rd.pk " +
            "JOIN BookEntity b ON b.pk = l.book.pk " +
            "JOIN GenreEntity g ON g.pk = b.genre.pk " +
            "WHERE g.genre = :genre " +
            "AND l.startDate >= :startDate " +
            "AND l.startDate <= :endDate " +
            // group by the whole entity (rd) so SQL Server doesn't require listing every column
            "GROUP BY rd " +
            "ORDER BY COUNT(l.pk) DESC")
    @Cacheable(key = "#genre + ':' + #startDate.toString() + ':' + #endDate.toString() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable,
                                            @Param("genre") String genre,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
