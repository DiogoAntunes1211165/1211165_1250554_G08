package pt.psoft.g1.psoftg1.genremanagement.repositories.relational;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;

import java.util.List;
import java.util.Optional;

public interface GenreRepositorySql extends CrudRepository<GenreEntity, Long> {

    @Query(value = "SELECT * FROM genre_entity", nativeQuery = true)
    List<GenreEntity> findAllGenres();

    @Query(value = "SELECT * FROM genre_Entity WHERE genre = :genreName", nativeQuery = true)
    Optional<GenreEntity> findByString(@Param("genreName") @NotNull String genre);

    @Query(value = "SELECT g.genre, COUNT(b.pk) as bookCount " +
            "FROM genre_entity g " +
            "JOIN BookEntity b ON b.genre_pk = g.pk " +
            "GROUP BY g.genre " +
            "ORDER BY COUNT(b.pk) DESC",
            countQuery = "SELECT COUNT(*) FROM genre",
            nativeQuery = true)
    Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);
}
