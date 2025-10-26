package pt.psoft.g1.psoftg1.shared.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Profile("mongodb")
@Repository("PhotoRepositoryMongoDBImpl")
public class PhotoRepositoryMongoDBImpl implements PhotoRepository {

    private final PhotoRepositoryMongoDB photoRepositoryMongoDB;

    @Autowired
    @Lazy
    public PhotoRepositoryMongoDBImpl(PhotoRepositoryMongoDB photoRepositoryMongoDB) {
        this.photoRepositoryMongoDB = photoRepositoryMongoDB;
    }

    @Override
    public void deleteByPhotoFile(String photoFile) {
        photoRepositoryMongoDB.deleteByPhotoFile(photoFile);
    }

}
