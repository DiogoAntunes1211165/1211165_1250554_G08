package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.FineEntity;

import java.util.Optional;


@Repository
@CacheConfig(cacheNames = "fines")
public interface FineRepositorySql extends CrudRepository<FineEntity, Integer> {

    @Cacheable(key = "#lendingNumber")
    @Query("SELECT f " +
            "FROM FineEntity f " +
            "JOIN LendingEntity l ON f.lendingEntity.pk = l.pk " +
            "WHERE l.lendingNumberEntity.lendingNumber = :lendingNumber")
    Optional<FineEntity> findByLendingNumber(String lendingNumber);
}
