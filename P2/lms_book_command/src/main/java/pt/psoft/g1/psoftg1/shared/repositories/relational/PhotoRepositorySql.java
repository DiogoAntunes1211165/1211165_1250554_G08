package pt.psoft.g1.psoftg1.shared.repositories.relational;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

public interface PhotoRepositorySql extends CrudRepository<PhotoEntity, Integer> {


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Photo WHERE photo_file = :photoFile", nativeQuery = true)
    void deleteByPhotoFile(@Param("photoFile") String photoFile);






}
