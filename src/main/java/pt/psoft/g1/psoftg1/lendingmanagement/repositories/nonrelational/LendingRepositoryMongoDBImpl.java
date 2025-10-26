package pt.psoft.g1.psoftg1.lendingmanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.nonrelational.BookDocumentPersistence;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.LendingDocument;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers.LendingDocumentMapper;
import pt.psoft.g1.psoftg1.readermanagement.repositories.nonrelational.ReaderDocumentPersistence;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("LendingRepositoryMongoDBImpl")
public class LendingRepositoryMongoDBImpl implements LendingRepository {

    private final LendingRepositoryMongoDB lendingRepository;
    private final LendingDocumentMapper lendingDocumentMapper;
    private final ReaderDocumentPersistence readerDocumentPersistence;
    private final BookDocumentPersistence bookDocumentPersistence;

    @Autowired
    @Lazy
    public LendingRepositoryMongoDBImpl(
            LendingRepositoryMongoDB lendingRepository,
            LendingDocumentMapper lendingDocumentMapper,
            ReaderDocumentPersistence readerDocumentPersistence,
            BookDocumentPersistence bookDocumentPersistence) {
        this.lendingRepository = lendingRepository;
        this.lendingDocumentMapper = lendingDocumentMapper;
        this.readerDocumentPersistence = readerDocumentPersistence;
        this.bookDocumentPersistence = bookDocumentPersistence;
    }

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        return lendingRepository.findByLendingNumber(lendingNumber)
                .map(lendingDocumentMapper::toDomain);
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        return lendingRepository.listByReaderNumberAndIsbn(readerNumber, isbn)
                .stream()
                .map(lendingDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public int getCountFromCurrentYear() {
        LocalDateTime startOfYear = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 31).atTime(23, 59, 59);
        return lendingRepository.countByStartDateBetween(startOfYear, endOfYear);
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        return lendingRepository.listOutstandingByReaderNumber(readerNumber)
                .stream()
                .map(lendingDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public Double getAverageDuration() {
        return lendingRepository.getAverageDuration();
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        return lendingRepository.getAvgLendingDurationByIsbn(isbn);
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        // Mongo não suporta paginação custom aqui sem Pageable, então ignora Page e retorna todos
        LocalDate now = LocalDate.now();
        return lendingRepository.findAll().stream()
                .filter(l -> l.getLimitDate() != null && l.getLimitDate().isBefore(now) && l.getReturnedDate() == null)
                .map(lendingDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned,
                                        LocalDate startDate, LocalDate endDate) {
        // Implementação básica — filtra manualmente, dado que as queries complexas não estão definidas
        return lendingRepository.findAll().stream()
                .filter(l -> readerNumber == null ||
                        (l.getReaderDetails() != null &&
                                l.getReaderDetails().getReaderNumber().toString().equals(readerNumber)))
                .filter(l -> isbn == null ||
                        (l.getBook() != null &&
                                l.getBook().getIsbn().getIsbn().equals(isbn)))
                .filter(l -> returned == null ||
                        (returned ? l.getReturnedDate() != null : l.getReturnedDate() == null))
                .filter(l -> startDate == null ||
                        (l.getStartDate() != null && !l.getStartDate().isBefore(startDate)))
                .filter(l -> endDate == null ||
                        (l.getStartDate() != null && !l.getStartDate().isAfter(endDate)))
                .map(lendingDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public Lending save(Lending lending) {
        LendingDocument doc = lendingDocumentMapper.toDocument(lending);
        LendingDocument saved = lendingRepository.save(doc);
        return lendingDocumentMapper.toDomain(saved);
    }

    @Override
    public void delete(Lending lending) {
        lendingRepository.deleteById(String.valueOf(lending.getPk()));
    }
}
