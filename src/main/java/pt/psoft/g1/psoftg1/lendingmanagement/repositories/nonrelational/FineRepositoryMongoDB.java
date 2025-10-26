package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.FineDocument;

import java.util.Optional;

@Repository
public interface FineRepositoryMongoDB extends MongoRepository<FineDocument, String> {

    @Query("{ 'lendingNumber' : ?0 }")
    Optional<FineDocument> findByLendingNumber(String lendingNumber);

}
