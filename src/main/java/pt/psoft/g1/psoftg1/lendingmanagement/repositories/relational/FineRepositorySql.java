package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.FineEntity;

import java.util.Optional;


public interface FineRepositorySql extends CrudRepository<FineEntity, Integer> {

    @Query(
            value = "SELECT f.* " +
                    "FROM fine_entity f " +
                    "JOIN lending_entity l ON f.lending_pk = l.pk " +
                    "JOIN lending_number_entity ln ON l.lending_number_pk = ln.pk " +
                    "WHERE ln.lending_number = :lendingNumber",
            nativeQuery = true
    )
    Optional<FineEntity> findByLendingNumber(String lendingNumber);
}
