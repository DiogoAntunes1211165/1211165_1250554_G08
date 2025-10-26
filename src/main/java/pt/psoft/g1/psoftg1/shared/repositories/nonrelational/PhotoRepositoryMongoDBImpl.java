package pt.psoft.g1.psoftg1.shared.repositories.nonrelational;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Profile("mongodb")
@Repository("PhotoRepositoryMongoDBImpl")
public class PhotoRepositoryMongoDBImpl implements PhotoRepository {

    private final PhotoRepositoryMongoDB photoRepositoryMongoDB;

    public PhotoRepositoryMongoDBImpl(PhotoRepositoryMongoDB photoRepositoryMongoDB) {
        this.photoRepositoryMongoDB = photoRepositoryMongoDB;
    }

    @Override
    public void deleteByPhotoFile(String photoFile) {
        photoRepositoryMongoDB.deleteByPhotoFile(photoFile);
    }

}
