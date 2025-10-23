package pt.psoft.g1.psoftg1.genremanagement.repositories.nonrelational;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;

import java.util.Optional;

@Repository
public interface GenreDocumentPersistence extends MongoRepository<GenreDocument, String> {

    // Busca um género pelo nome
    @Query("{ 'genre': ?0 }")
    Optional<GenreDocument> findByString(String genreName);
}
