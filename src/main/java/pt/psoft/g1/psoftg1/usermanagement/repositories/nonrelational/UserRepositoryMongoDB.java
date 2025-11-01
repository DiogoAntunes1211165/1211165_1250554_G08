package pt.psoft.g1.psoftg1.usermanagement.repositories.nonrelational;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.UserDocument;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepositoryMongoDB extends MongoRepository<UserDocument, Long> {


    @Query("{ '_id': ?0 }")
    Optional<UserDocument> findById(Long id);

    @Query("{ 'username': ?0 }")
    Optional<UserDocument> findByUsername(String username);


    @Query("{ 'name': ?0 }")
    List<UserDocument> findByName(String name);


    @Override
    <S extends UserDocument> S save(S entity);
}
