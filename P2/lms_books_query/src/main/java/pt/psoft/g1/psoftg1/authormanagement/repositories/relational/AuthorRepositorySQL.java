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


@CacheConfig(cacheNames = {"authors"})
public interface AuthorRepositorySQL extends CrudRepository<AuthorEntity, Long> {

    // ---- SAVE ----
    // Salvar ou atualizar autor limpa o cache relacionado a autores
    @Override
    @CacheEvict(value = {
            "authorsById",
            "authorsByNumber",
            "authorsByNameStart",
            "authorsByExactName",
            "coAuthors",
            "topAuthorsByLendings"
    }, allEntries = true)
    <S extends AuthorEntity> S save(S entity);

    @Override
    @CacheEvict(value = {
            "authorsById",
            "authorsByNumber",
            "authorsByNameStart",
            "authorsByExactName",
            "coAuthors",
            "topAuthorsByLendings"
    }, allEntries = true)
    <S extends AuthorEntity> List<S> saveAll(Iterable<S> entities);

    // ---- FIND BY ID ----
    @Override
    @Cacheable(value = "authorsById", key = "#id")
    Optional<AuthorEntity> findById(Long id);

    // ---- FIND BY AUTHOR NUMBER ----
    @Cacheable(value = "authorsByNumber", key = "#authorNumber")
    @Query(value = "SELECT * FROM author WHERE author_number = :authorNumber", nativeQuery = true)
    Optional<AuthorEntity> findByAuthorNumber(String authorNumber);

    // ---- FIND BY NAME START ----
    @Cacheable(value = "authorsByNameStart", key = "#name")
    @Query(value = "SELECT * FROM author a WHERE a.name LIKE CONCAT(:name, '%')", nativeQuery = true)
    List<AuthorEntity> findByNameStartsWith(String name);

    // ---- FIND BY EXACT NAME ----
    @Cacheable(value = "authorsByExactName", key = "#name")
    @Query(value = "SELECT * FROM author a WHERE a.name = :name", nativeQuery = true)
    List<AuthorEntity> searchByNameName(String name);

    // ---- CO-AUTHORS ----
    @Cacheable(value = "coAuthors", key = "#authorNumber")
    @Query(value = "SELECT DISTINCT co.* FROM book_author ba " +
            "JOIN author_entity co ON ba.author_id = co.id " +
            "WHERE ba.book_id IN ( " +
            "  SELECT ba2.book_id FROM book_author ba2 WHERE ba2.author_id = :authorNumber " +
            ") AND co.author_number <> :authorNumber",
            nativeQuery = true)
    List<AuthorEntity> findCoAuthorsByAuthorNumber(Long authorNumber);

    // ---- COUNT ----
    @Override
    @Cacheable(value = "authorsCount")
    long count();

    // ---- TOP AUTHORS BY LENDINGS ----
    @Cacheable(value = "topAuthorsByLendings", key = "#pageable.pageNumber")
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
