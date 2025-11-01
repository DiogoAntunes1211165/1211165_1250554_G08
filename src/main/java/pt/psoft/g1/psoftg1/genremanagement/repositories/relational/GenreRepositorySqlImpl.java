package pt.psoft.g1.psoftg1.genremanagement.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.mappers.GenreEntityMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Repository("GenreRepositorySqlImpl")
@Profile("sqlServer")
@Transactional
public class GenreRepositorySqlImpl implements GenreRepository {
    private final GenreRepositorySql genreRepositorySql;
    private final GenreEntityMapper genreEntityMapper;

    @Autowired
    @Lazy
    public GenreRepositorySqlImpl(GenreRepositorySql genreRepositorySql, GenreEntityMapper genreEntityMapper) {
        this.genreRepositorySql = genreRepositorySql;
        this.genreEntityMapper = genreEntityMapper;
    }

    @Override
    public Iterable<Genre> findAll() {
        ArrayList<Genre> genres = new ArrayList<>();
        for (GenreEntity entity : genreRepositorySql.findAllGenres()) {
            genres.add(genreEntityMapper.toDomain(entity));
        }
        return genres;
        }

    @Override
    public Optional<Genre> findByString(String genreName) {
        // Call the underlying repository only once to avoid duplicate cache lookups and potential race with cached empty values.
        Optional<GenreEntity> opt = genreRepositorySql.findByString(genreName);
        if (opt.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(genreEntityMapper.toDomain(opt.get()));
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allGenres", allEntries = true),
            @CacheEvict(value = "genreByName", key = "#genre.genre")
    })
    public Genre save(Genre genre) {
        GenreEntity entity = genreEntityMapper.toEntity(genre); // Convert Genre to GenreEntity
        GenreEntity saved = genreRepositorySql.save(entity); // Save the entity
        return genreEntityMapper.toDomain(saved); // Convert back to Genre and return
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allGenres", allEntries = true),
            @CacheEvict(value = "genreByName", key = "#genre.genre")
    })
    public void delete(Genre genre) {
        genreRepositorySql.delete(genreEntityMapper.toEntity(genre));
    }

    @Override
    public Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) {
        return genreRepositorySql.findTop5GenreByBookCount(pageable);
    }

    @Override
    public List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page) {
        return List.of();
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
        return List.of();
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}
