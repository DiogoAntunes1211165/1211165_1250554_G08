package pt.psoft.g1.psoftg1.shared.repositories.nonrelational;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.PhotoDocument;

public interface PhotoDocumentPersistence extends MongoRepository<PhotoDocument, String> {

    @Query(value = "{ 'photo_file': ?0 }", delete = true)
    void deleteByPhotoFile(String photoFile);

}
