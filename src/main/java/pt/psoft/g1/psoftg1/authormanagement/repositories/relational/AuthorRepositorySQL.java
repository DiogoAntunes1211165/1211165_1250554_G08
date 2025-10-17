package pt.psoft.g1.psoftg1.authormanagement.repositories.relational;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;

import java.util.List;
import java.util.Optional;


@CacheConfig(cacheNames = "authors")
public interface AuthorRepositorySQL extends CrudRepository<AuthorEntity, Long> {

    // Salvar ou atualizar autor limpa o cache do autor específico
    @Override
    @CacheEvict(allEntries = true)
    <S extends AuthorEntity> S save(S entity);

    @Override
    @CacheEvict(allEntries = true)
    <S extends AuthorEntity> List<S> saveAll(Iterable<S> entities);

    @Override
    @Cacheable
    Optional<AuthorEntity> findById(Long id);

    // Busca por authorNumber usando SQL nativo
    @Cacheable
    @Query(value = "SELECT * FROM author WHERE author_number = :authorNumber", nativeQuery = true)
    Optional<AuthorEntity> findByAuthorNumber(String authorNumber);

    // Busca autores cujo nome começa com string fornecida
    @Cacheable
    @Query(value = "SELECT * FROM author a WHERE a.name LIKE :name + '%'", nativeQuery = true)
    List<AuthorEntity> findByNameStartsWith(String name);

    // Busca autores com nome exato
    @Cacheable
    @Query(value = "SELECT * FROM author a WHERE a.name = :name", nativeQuery = true)
    List<AuthorEntity> searchByNameName(String name);


    // Coautores (SQL nativo)
    @Cacheable
    @Query(value = "SELECT DISTINCT co.* FROM book_author ba " +
            "JOIN author_entity co ON ba.author_id = co.id " +
            "WHERE ba.book_id IN ( " +
            "  SELECT ba2.book_id FROM book_author ba2 WHERE ba2.author_id = :authorNumber " +
            ") AND co.author_number <> :authorNumber", nativeQuery = true)
    List<AuthorEntity> findCoAuthorsByAuthorNumber(Long authorNumber);

    // Top autores por empréstimos (SQL nativo)
    @Cacheable
    @Query(value = "SELECT a.name AS name, COUNT(l.id) AS lendCount " +
            "FROM book_entity b " +
            "JOIN book_author ba ON b.id = ba.book_id " +
            "JOIN author_entity a ON ba.author_id = a.id " +
            "JOIN lending_entity l ON l.book_id = b.id " +
            "GROUP BY a.name " +
            "ORDER BY lendCount DESC",
            countQuery = "SELECT COUNT(*) FROM author_entity",
            nativeQuery = true)
    Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable);

}
