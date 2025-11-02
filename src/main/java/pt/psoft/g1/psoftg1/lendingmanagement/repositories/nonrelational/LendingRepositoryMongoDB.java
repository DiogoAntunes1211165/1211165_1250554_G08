package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.LendingDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface LendingRepositoryMongoDB extends MongoRepository<LendingDocument, String> {

    // Equivalente de findByLendingNumber
    @Query("{ 'lending_number.lending_number' : ?0 }")
    Optional<LendingDocument> findByLendingNumber(String lendingNumber);


    // Equivalente de listByReaderNumberAndIsbn
    @Query("{ 'book.isbn.isbn' : ?1, 'readerDetails.readerNumber.readerNumber' : ?0 }")
    List<LendingDocument> listByReaderNumberAndIsbn(String readerNumber, String isbn);


    // Equivalente de getCountFromCurrentYear
    // Mongo não tem YEAR() nativo, então precisa comparar datas por range
    @Query("{ 'startDate' : { $gte: ?0, $lt: ?1 } }")
    int countByStartDateBetween(java.time.LocalDateTime startOfYear, java.time.LocalDateTime endOfYear);


    // Equivalente de listOutstandingByReaderNumber
    @Query("{ 'readerDetails.readerNumber.readerNumber' : ?0, 'returnedDate' : null }")
    List<LendingDocument> listOutstandingByReaderNumber(String readerNumber);


    // Equivalente de getAverageDuration
    // precisa de agregação, então aqui só declaramos — usa @Aggregation
    @Aggregation(pipeline = {
            "{ $match: { returnedDate: { $ne: null } } }",
            "{ $project: { durationDays: { $divide: [ { $subtract: ['$returnedDate', '$startDate'] }, 1000*60*60*24 ] } } }",
            "{ $group: { _id: null, avgDuration: { $avg: '$durationDays' } } }"
    })
    Double getAverageDuration();


    // Equivalente de getAvgLendingDurationByIsbn
    @Aggregation(pipeline = {
            "{ $match: { returnedDate: { $ne: null }, 'book.isbn.isbn': ?0 } }",
            "{ $project: { durationDays: { $divide: [ { $subtract: ['$returnedDate', '$startDate'] }, 1000*60*60*24 ] } } }",
            "{ $group: { _id: null, avgDuration: { $avg: '$durationDays' } } }"
    })
    Double getAvgLendingDurationByIsbn(String isbn);

}
