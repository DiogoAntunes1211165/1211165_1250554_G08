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
    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.authorities WHERE u.id = ?1")
    Optional<UserEntity> findById(Long id);

    @Cacheable(key = "#username")
    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.authorities WHERE u.username = ?1")
    Optional<UserEntity> findByUsername(String username);

    @Cacheable(key = "#name")
    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.authorities WHERE u.name = ?1")
    List<UserEntity> findByName(String name);

    @CacheEvict(allEntries = true)
    <S extends UserEntity> S save(S entity);
}
