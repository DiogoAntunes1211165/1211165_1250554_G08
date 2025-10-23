package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

import java.util.Optional;

@Profile("mongodb")
@Repository("FineRepositoryMongoDBImpl")
public class FineRepositoryMongoDBImpl implements FineRepository {
    @Override
    public Optional<Fine> findByLendingNumber(String lendingNumber) {
        return Optional.empty();
    }

    @Override
    public Iterable<Fine> findAll() {
        return null;
    }

    @Override
    public Fine save(Fine fine) {
        return null;
    }
}
