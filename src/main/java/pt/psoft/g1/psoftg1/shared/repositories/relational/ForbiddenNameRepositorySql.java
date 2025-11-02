package pt.psoft.g1.psoftg1.shared.repositories.relational;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.shared.model.relational.ForbiddenNameEntity;

import java.util.List;
import java.util.Optional;

public interface ForbiddenNameRepositorySql extends CrudRepository<ForbiddenNameEntity, Integer> {

    @Query(value = "SELECT * FROM forbidden_name_entity WHERE :pat LIKE CONCAT('%', forbidden_name, '%')", nativeQuery = true)
    List<ForbiddenNameEntity> findByForbiddenNameIsContained(@Param("pat") String pat);

    @Query(value = "SELECT * FROM forbidden_name_entity WHERE forbidden_name = :forbiddenName", nativeQuery = true)
    Optional<ForbiddenNameEntity> findByForbiddenName(@Param("forbiddenName") String forbiddenName);

    @Modifying
    @Query(value = "DELETE FROM forbidden_name_entity WHERE forbidden_name = :forbiddenName", nativeQuery = true)
    int deleteForbiddenName(@Param("forbiddenName") String forbiddenName);





}
