package pt.psoft.g1.psoftg1.authormanagement.repositories.nonrelational;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
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
    @Query("{ 'name.name': { $regex: '^?0', $options: 'i' } }")
    List<AuthorDocument> findByNameStartsWith(String name);

    // Busca autores com nome exato
    @Cacheable
    @Query("{ 'name.name': ?0 }")
    List<AuthorDocument> findByName(String name);

    /* // Coautores — autores que participaram nos mesmos livros que um dado autor
    @Aggregation(pipeline = {
            "{ $match: { 'authorNumber': ?0 } }",
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'authors._id', as: 'books' } }",
            "{ $unwind: '$books' }",
            "{ $lookup: { from: 'author', localField: 'books.authors._id', foreignField: '_id', as: 'coauthors' } }",
            "{ $unwind: '$coauthors' }",
            "{ $match: { 'coauthors.authorNumber': { $ne: ?0 } } }",
            "{ $group: { _id: '$coauthors._id', author: { $first: '$coauthors' } } }",
            "{ $replaceRoot: { newRoot: '$author' } }"
    })
    List<AuthorDocument> findCoAuthorsByAuthorNumber(String authorNumber);


    // Top autores por número de empréstimos
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'authors._id', as: 'books' } }",
            "{ $unwind: '$books' }",
            "{ $lookup: { from: 'lending', localField: 'books._id', foreignField: 'bookId', as: 'lendings' } }",
            "{ $unwind: '$lendings' }",
            "{ $group: { _id: '$_id', lendingCount: { $sum: 1 }, author: { $first: '$$ROOT' } } }",
            "{ $sort: { lendingCount: -1 } }"
    })
    Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable); */

}
