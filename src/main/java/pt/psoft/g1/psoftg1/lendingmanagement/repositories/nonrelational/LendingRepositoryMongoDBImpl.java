package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("LendingRepositoryMongoDBImpl")
public class LendingRepositoryMongoDBImpl implements LendingRepository {
    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        return Optional.empty();
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        return List.of();
    }

    @Override
    public int getCountFromCurrentYear() {
        return 0;
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        return List.of();
    }

    @Override
    public Double getAverageDuration() {
        return 0.0;
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        return 0.0;
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        return List.of();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public Lending save(Lending lending) {
        return null;
    }

    @Override
    public void delete(Lending lending) {

    }
}
