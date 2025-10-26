package pt.psoft.g1.psoftg1.shared.repositories.nonrelational;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.ForbiddenNameDocument;

import java.util.List;
import java.util.Optional;

public interface ForbiddenNameRepositoryMongoDB extends MongoRepository<ForbiddenNameDocument, String> {

    @Query("{ 'forbidden_name': { $regex: ?0, $options: 'i' } }")
    List<ForbiddenNameDocument> findByForbiddenNameIsContained(String pat);

    @Query ("{ 'forbidden_name': ?0 }")
    Optional<ForbiddenNameDocument> findByForbiddenName(String forbiddenName);

    void deleteByForbiddenName(String forbiddenName);
}
