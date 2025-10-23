package pt.psoft.g1.psoftg1.authormanagement.repositories.nonrelational;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.AuthorDocument;

import java.util.List;
import java.util.Optional;

@CacheConfig(cacheNames = "authors")
@Repository
public interface AuthorMongoDBPersistence extends MongoRepository<AuthorDocument, String> {

    // Salvar ou atualizar limpa o cache
    @Override
    @CacheEvict(allEntries = true)
    <S extends AuthorDocument> S save(S entity);

    @Override
    @CacheEvict(allEntries = true)
    <S extends AuthorDocument> List<S> saveAll(Iterable<S> entities);

    @Override
    @Cacheable
    Optional<AuthorDocument> findById(String id);

    // Busca por authorNumber
    @Cacheable
    @Query("{ 'authorNumber': ?0 }")
    Optional<AuthorDocument> findByAuthorNumber(String authorNumber);

    // Busca autores cujo nome começa com string fornecida
    @Cacheable
    @Query("{ 'name': { $regex: '^?0', $options: 'i' } }")
    List<AuthorDocument> findByNameStartsWith(String name);

    // Busca autores com nome exato
    @Cacheable
    @Query("{ 'name': ?0 }")
    List<AuthorDocument> findByName(String name);

    // Coautores — este tipo de query complexa não existe diretamente em Mongo sem modelagem relacional,
    // então aqui devolvemos vazio por padrão, ou poderias modelar manualmente numa query agregada.
    default List<AuthorDocument> findCoAuthorsByAuthorNumber(String authorNumber) {
        return List.of();
    }

    // Top autores por empréstimos — também não é direto em Mongo, a menos que cries uma coleção
    // ou pipeline agregador específico. Colocamos o método para manter interface consistente.
    default Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented for MongoDB");
    }
}
