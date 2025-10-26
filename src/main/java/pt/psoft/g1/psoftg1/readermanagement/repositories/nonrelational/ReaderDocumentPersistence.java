package pt.psoft.g1.psoftg1.readermanagement.repositories.nonrelational;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderDetailsDocument;

import java.util.List;
import java.util.Optional;

public interface ReaderDocumentPersistence extends MongoRepository<ReaderDetailsDocument, String> {

    // Busca por número de leitor
    @Query ("{ 'reader_number.reader_number': ?0 }")
    Optional<ReaderDetailsDocument> findByReaderNumber(String readerNumber);

    // Busca por número de telefone
    List<ReaderDetailsDocument> findByPhoneNumber(String phoneNumber);

    // Busca por username (via campo aninhado no documento Reader)
    @Query("{ 'reader.username': ?0 }")
    Optional<ReaderDetailsDocument> findByReader_Username(String username);

    @Query("{ 'reader.email': ?0 }")
    Optional<ReaderDetailsDocument> findByReader_Email(String email);

    // Busca por ID do utilizador (Reader)
    @Query("{ 'reader.id': ?0 }")
    Optional<ReaderDetailsDocument> findByUserId(String userId);

    // Contar leitores criados no ano corrente
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'reader', localField: 'reader', foreignField: '_id', as: 'readerData' } }",
            "{ $unwind: '$readerData' }",
            "{ $match: { $expr: { $eq: [ { $year: '$readerData.createdAt' }, { $year: new Date() } ] } } }",
            "{ $count: 'total' }"
    })
    int getCountFromCurrentYear();

    /* // Top leitores com mais empréstimos
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'lending', localField: '_id', foreignField: 'readerDetailsId', as: 'lendings' } }",
            "{ $addFields: { lendingCount: { $size: '$lendings' } } }",
            "{ $sort: { lendingCount: -1 } }"
    })
    Page<ReaderDetailsDocument> findTopReaders(Pageable pageable);

    // Top leitores por género entre datas
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'lending', localField: '_id', foreignField: 'readerDetailsId', as: 'lendings' } }",
            "{ $unwind: '$lendings' }",
            "{ $lookup: { from: 'book', localField: 'lendings.bookId', foreignField: '_id', as: 'book' } }",
            "{ $unwind: '$book' }",
            "{ $lookup: { from: 'genre', localField: 'book.genreId', foreignField: '_id', as: 'genre' } }",
            "{ $unwind: '$genre' }",
            "{ $match: { 'genre.genre': ?0, 'lendings.startDate': { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: '$_id', lendingCount: { $sum: 1 }, doc: { $first: '$$ROOT' } } }",
            "{ $sort: { lendingCount: -1 } }"
    })
    Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate); */

}
