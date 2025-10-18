package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers.FineEntityMapper;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository("fineRepositorySqlImpl")
@Profile("sqlServer")
public class FineRepositorySqlImpl implements FineRepository {

    private final FineRepositorySql fineRepositoryJpa;
    private final FineEntityMapper fineMapper;


    @Autowired
    @Lazy
    public FineRepositorySqlImpl(FineRepositorySql fineRepositoryJpa, FineEntityMapper fineMapper) {
        this.fineRepositoryJpa = fineRepositoryJpa;
        this.fineMapper = fineMapper;
    }


    @Override
    public Optional<Fine> findByLendingNumber(String lendingNumber) {
        if (fineRepositoryJpa.findByLendingNumber(lendingNumber).isEmpty()) {
            return Optional.empty();
        } else {
            Fine fine = fineMapper.toDomain(fineRepositoryJpa.findByLendingNumber(lendingNumber).get());
            return Optional.of(fine);
        }
    }

    @Override
    public Iterable<Fine> findAll() {
        return StreamSupport.stream(fineRepositoryJpa.findAll().spliterator(), false)
                .map(fineMapper::toDomain)
                .toList();
    }

    @Override
    public Fine save(Fine fine) {
        var fineEntity = fineMapper.toEntity(fine);
        var savedEntity = fineRepositoryJpa.save(fineEntity);
        return fineMapper.toDomain(savedEntity);
    }
}
