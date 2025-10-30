package pt.psoft.g1.psoftg1.usermanagement.repositories.relational;



import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.UserEntity;

import java.util.List;
import java.util.Optional;


@CacheConfig(cacheNames = "users")
public interface UserRepositorySQL extends CrudRepository<UserEntity, Long> {

    @Cacheable(key = "#id")
    @Query(value = "SELECT * FROM T_USER WHERE id = ?1", nativeQuery = true)
    Optional<UserEntity> findById(Long id);

    @Cacheable(key = "#username")
    @Query(value = "SELECT * FROM T_USER WHERE username = ?1", nativeQuery = true)
    Optional<UserEntity> findByUsername(String username);

    @Cacheable(key = "#name")
    @Query(value = "SELECT * FROM T_USER WHERE name = ?1", nativeQuery = true)
    List<UserEntity> findByName(String name);

    @CacheEvict(allEntries = true)
    <S extends UserEntity> S save(S entity);
}
