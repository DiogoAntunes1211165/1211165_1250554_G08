package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.FineDocument;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers.FineDocumentMapper;

import java.util.Optional;

@Profile("mongodb")
@Repository("FineRepositoryMongoDBImpl")
public class FineRepositoryMongoDBImpl implements FineRepository {

    private final FineRepositoryMongoDB fineRepositoryMongoDB;
    private final FineDocumentMapper fineDocumentMapper;

    @Autowired
    @Lazy
    public FineRepositoryMongoDBImpl(FineRepositoryMongoDB fineRepositoryMongoDB,
                                     FineDocumentMapper fineDocumentMapper) {
        this.fineRepositoryMongoDB = fineRepositoryMongoDB;
        this.fineDocumentMapper = fineDocumentMapper;
    }

    @Override
    public Optional<Fine> findByLendingNumber(String lendingNumber) {
        Optional<FineDocument> fineMongoDB = fineRepositoryMongoDB.findByLendingNumber(lendingNumber);
        if(fineMongoDB.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(fineDocumentMapper.toDomain(fineMongoDB.get()));
        }
    }

    @Override
    public Iterable<Fine> findAll() {
        return fineRepositoryMongoDB.findAll()
                .stream()
                .map(fineDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public Fine save(Fine fine) {
        FineDocument fineMonghoDB = fineDocumentMapper.toDocument(fine);
        FineDocument savedEntity = fineRepositoryMongoDB.save(fineMonghoDB);
        return fineDocumentMapper.toDomain(savedEntity);
    }
}
