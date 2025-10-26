package pt.psoft.g1.psoftg1.shared.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.ForbiddenNameDocument;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.repositories.mappers.ForbiddenNameDocumentMapper;

import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("ForbiddenNameRepositoryMongoDBImpl")
public class ForbiddenNameRepositoryMongoDBImpl implements ForbiddenNameRepository {

    private final ForbiddenNameRepositoryMongoDB forbiddenNameRepositoryMongoDB;
    private final ForbiddenNameDocumentMapper forbiddenNameDocumentMapper;

    @Autowired
    @Lazy
    public ForbiddenNameRepositoryMongoDBImpl(ForbiddenNameRepositoryMongoDB forbiddenNameRepositoryMongoDB, ForbiddenNameDocumentMapper forbiddenNameDocumentMapper) {
        this.forbiddenNameRepositoryMongoDB = forbiddenNameRepositoryMongoDB;
        this.forbiddenNameDocumentMapper = forbiddenNameDocumentMapper;
    }

    @Override
    public Iterable<ForbiddenName> findAll() {
        Iterable<ForbiddenNameDocument> documents = forbiddenNameRepositoryMongoDB.findAll();
        List<ForbiddenName> result = new java.util.ArrayList<>();
        for (ForbiddenNameDocument document : documents) {
            result.add(forbiddenNameDocumentMapper.toModel(document));
        }
        return result;
    }

    @Override
    public List<ForbiddenName> findByForbiddenNameIsContained(String pat) {
        List<ForbiddenNameDocument> documents = forbiddenNameRepositoryMongoDB.findByForbiddenNameIsContained(pat);
        List<ForbiddenName> result = new java.util.ArrayList<>();
        for (ForbiddenNameDocument document : documents) {
            result.add(forbiddenNameDocumentMapper.toModel(document));
        }
        return result;
    }

    @Override
    public ForbiddenName save(ForbiddenName forbiddenName) {
        ForbiddenNameDocument document = forbiddenNameDocumentMapper.toDocument(forbiddenName);
        ForbiddenNameDocument saved = forbiddenNameRepositoryMongoDB.save(document);
        return forbiddenNameDocumentMapper.toModel(saved);
    }

    @Override
    public Optional<ForbiddenName> findByForbiddenName(String forbiddenName) {
        ForbiddenNameDocument forbiddenNameMongoDB = forbiddenNameRepositoryMongoDB.findByForbiddenName(forbiddenName).orElse(null);
        if(forbiddenNameMongoDB == null) {
            return Optional.empty();
        }
        return Optional.of(forbiddenNameDocumentMapper.toModel(forbiddenNameMongoDB));
    }

    @Override
    public int deleteForbiddenName(String forbiddenName) {
        forbiddenNameRepositoryMongoDB.deleteByForbiddenName(forbiddenName);
        return 1;
    }

}
