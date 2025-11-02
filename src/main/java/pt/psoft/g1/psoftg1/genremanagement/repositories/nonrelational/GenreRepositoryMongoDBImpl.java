package pt.psoft.g1.psoftg1.genremanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.mappers.GenreDocumentMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("GenreRepositoryMongoDBImpl")
public class GenreRepositoryMongoDBImpl implements GenreRepository {

    private final GenreRepositoryMongoDB genreRepositoryMongoDB;
    private final GenreDocumentMapper genreDocumentMapper;

    @Autowired
    @Lazy
    public GenreRepositoryMongoDBImpl(GenreRepositoryMongoDB genreRepositoryMongoDB, GenreDocumentMapper genreDocumentMapper) {
        this.genreRepositoryMongoDB = genreRepositoryMongoDB;
        this.genreDocumentMapper = genreDocumentMapper;
    }

    @Override
    public Iterable<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        genreRepositoryMongoDB.findAll().forEach(doc -> genres.add(genreDocumentMapper.toDomain(doc)));
        return genres;
    }

    @Override
    public Optional<Genre> findByString(String genreName) {
        Optional<GenreDocument> doc = genreRepositoryMongoDB.findByString(genreName);
        return doc.map(genreDocumentMapper::toDomain);
    }

    @Override
    public Genre save(Genre genre) {
        GenreDocument doc = genreDocumentMapper.toDocument(genre);
        GenreDocument saved = genreRepositoryMongoDB.save(doc);
        return genreDocumentMapper.toDomain(saved);
    }

    @Override
    public void delete(Genre genre) {
        genreRepositoryMongoDB.delete(genreDocumentMapper.toDocument(genre));
    }

    // Mongo não tem estas queries agregadas ainda — devolvemos placeholders
    @Override
    public Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented for MongoDB");
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
