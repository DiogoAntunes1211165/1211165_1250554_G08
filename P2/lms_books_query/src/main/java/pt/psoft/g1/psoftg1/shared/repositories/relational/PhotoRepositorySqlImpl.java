package pt.psoft.g1.psoftg1.shared.repositories.relational;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Profile("sqlServer")
@Repository("PhotoRepositorySqlImpl")
public class PhotoRepositorySqlImpl implements PhotoRepository {

    private final PhotoRepositorySql photoRepositorySql;

    @Autowired
    @Lazy
    public PhotoRepositorySqlImpl(PhotoRepositorySql photoRepositorySql) {
        this.photoRepositorySql = photoRepositorySql;
    }

    @Override
    public void deleteByPhotoFile(String photoFile) {
        photoRepositorySql.deleteByPhotoFile(photoFile);
    }
}
