package pt.psoft.g1.psoftg1.shared.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.shared.model.relational.ForbiddenNameEntity;
import pt.psoft.g1.psoftg1.shared.repositories.mappers.ForbiddenNameEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

import java.util.List;
import java.util.Optional;

@Profile("sqlServer")
@Repository("ForbiddenNameRepositorySqlImpl")
public class ForbiddenNameRepositorySqlImpl implements ForbiddenNameRepository {
    private final ForbiddenNameRepositorySql forbiddenNameRepositorySql;
    private final ForbiddenNameEntityMapper forbiddenNameEntityMapper;

    @Autowired
    @Lazy
    public ForbiddenNameRepositorySqlImpl(ForbiddenNameRepositorySql forbiddenNameRepositorySql, ForbiddenNameEntityMapper forbiddenNameEntityMapper) {
        this.forbiddenNameRepositorySql = forbiddenNameRepositorySql;
        this.forbiddenNameEntityMapper = forbiddenNameEntityMapper;
    }

    @Override
    public Iterable<ForbiddenName> findAll() {
        Iterable<ForbiddenNameEntity> entities = forbiddenNameRepositorySql.findAll();
        List<ForbiddenName> result = new java.util.ArrayList<>();
        for (ForbiddenNameEntity entity : entities) {
            result.add(forbiddenNameEntityMapper.toModel(entity));
        }
        return result;
    }

    @Override
    public List<ForbiddenName> findByForbiddenNameIsContained(String pat) {
        List<ForbiddenNameEntity> entities = forbiddenNameRepositorySql.findByForbiddenNameIsContained(pat);
        List<ForbiddenName> result = new java.util.ArrayList<>();
        for (ForbiddenNameEntity entity : entities) {
            result.add(forbiddenNameEntityMapper.toModel(entity));
        }
        return result;
    }

    @Override
    public ForbiddenName save(ForbiddenName forbiddenName) {
        ForbiddenNameEntity entity = forbiddenNameEntityMapper.toEntity(forbiddenName);
        ForbiddenNameEntity saved = forbiddenNameRepositorySql.save(entity);
        return forbiddenNameEntityMapper.toModel(saved);
    }

    @Override
    public Optional<ForbiddenName> findByForbiddenName(String forbiddenName) {
        return forbiddenNameRepositorySql.findByForbiddenName(forbiddenName).map(forbiddenNameEntityMapper::toModel);
    }

    @Override
    public int deleteForbiddenName(String forbiddenName) {
        return forbiddenNameRepositorySql.deleteForbiddenName(forbiddenName);
    }
}
