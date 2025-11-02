package pt.psoft.g1.psoftg1.genremanagement.repositories.relational;

import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "allGenres")
    @Query(value = "SELECT * FROM genre_entity", nativeQuery = true)
    List<GenreEntity> findAllGenres();

    // Robust SpEL: handle both Optional return and direct entity gracefully (avoid calling isEmpty on entity)
    @Cacheable(value = "genreByName", key = "#p0", unless = "#result == null || (#result instanceof T(java.util.Optional) && !#result.isPresent())")
    @Query(value = "SELECT * FROM genre_entity WHERE genre = :genreName", nativeQuery = true)
    Optional<GenreEntity> findByString(@Param("genreName") @NotNull String genre);


    // Use JPQL to avoid depending on physical table names (works across dialects)
    @Cacheable(value = "topGenresByBookCount", key = "#pageable")
    @Query(value = "SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO(b.genre.genre, COUNT(b)) " +
            "FROM pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity b " +
            "GROUP BY b.genre.genre " +
            "ORDER BY COUNT(b) DESC")
    Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);
}
