package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.relational.BookRepositorySQL;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational.LendingRepositorySQL;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.relational.ReaderRepositorySQL;

import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers.LendingEntityMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sqlServer")
@Repository("LendingRepositorySQLImpl")
public class LendingRepositorySQLImpl implements LendingRepository {

    private final LendingRepositorySQL repository;
    private final LendingEntityMapper lendingEntityMapper;
    private final ReaderRepositorySQL readerDetailsRepository;
    private final BookRepositorySQL bookRepository;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    @Lazy
    public LendingRepositorySQLImpl(LendingRepositorySQL repository, LendingEntityMapper lendingEntityMapper, ReaderRepositorySQL readerDetailsRepository, BookRepositorySQL bookRepository, EntityManager em) {
        this.repository = repository;
        this.lendingEntityMapper = lendingEntityMapper;
        this.readerDetailsRepository = readerDetailsRepository;
        this.bookRepository = bookRepository;
        this.em = em;
    }




    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        // use mapper to convert LendingEntity to Lending
        if (repository.findByLendingNumber(lendingNumber).isEmpty()) {
            return Optional.empty();
        }else {
            Lending lending = lendingEntityMapper.toDomain(repository.findByLendingNumber(lendingNumber).get());
            return Optional.of(lending);
        }

    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        // Exemplo de delegação de uma busca

        List <Lending> lendings = new ArrayList<>();

        for (LendingEntity lendingEntity : repository.listByReaderNumberAndIsbn(readerNumber, isbn)) {
            lendings.add(lendingEntityMapper.toDomain(lendingEntity));
        }

        return lendings;

    }

    @Override
    public int getCountFromCurrentYear() {
        // Exemplo de lógica customizada
        return this.repository.getCountFromCurrentYear();
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        List <Lending> lendings = new ArrayList<>();

        for (LendingEntity lendingEntity : repository.listOutstandingByReaderNumber(readerNumber)) {
            lendings.add(lendingEntityMapper.toDomain(lendingEntity));
        }

        return lendings;

    }

    @Override
    public Double getAverageDuration() {
        return this.repository.getAverageDuration();
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        return this.repository.getAvgLendingDurationByIsbn(isbn);
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        final Root<LendingEntity> root = cq.from(LendingEntity.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        // Select overdue lendings where returnedDate is null and limitDate is before the current date
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate"))); // Order by limitDate, oldest first

        final TypedQuery<LendingEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<Lending> lendings = new ArrayList<>();

        for (LendingEntity lendingEntity : q.getResultList()) {
            lendings.add(lendingEntityMapper.toDomain(lendingEntity));
        }

        return lendings;
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        final Root<LendingEntity> lendingRoot = cq.from(LendingEntity.class);
        final Join<LendingEntity, Book> bookJoin = lendingRoot.join("book");
        final Join<LendingEntity, ReaderDetails> readerDetailsJoin = lendingRoot.join("readerDetails");
        cq.select(lendingRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(readerNumber))
            where.add(cb.like(readerDetailsJoin.get("readerNumber").get("readerNumber"), readerNumber));
        if (StringUtils.hasText(isbn))
            where.add(cb.like(bookJoin.get("isbn").get("isbn"), isbn));
        if (returned != null){
            if(returned){
                where.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            }else{
                where.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if(startDate!=null)
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        if(endDate!=null)
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        final TypedQuery<LendingEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<Lending> lendings = new ArrayList<>();

        for (LendingEntity lendingEntity : q.getResultList()) {
            lendings.add(lendingEntityMapper.toDomain(lendingEntity));
        }

        return lendings;
    }

    @Override
    public Lending save(Lending lending) {

        LendingEntity entity = lendingEntityMapper.toEntity(lending);


        if (entity.getBook() != null && entity.getBook().getIsbn() != null) {
            Optional<BookEntity> existingBookOptional = bookRepository.findByIsbn(entity.getBook().getIsbn());
            if (existingBookOptional.isPresent()) {
                // Se o livro já existe, utiliza o livro existente
                entity.setBook(existingBookOptional.get());
            } else {
                // Se o livro não existe, salva o novo livro
                BookEntity savedBook = bookRepository.save(entity.getBook());
                entity.setBook(savedBook);
            }
        }

        if(entity.getReaderDetails() != null && entity.getReaderDetails().getReaderNumber() != null){
            Optional<ReaderDetailsEntity> existingReaderDetailsOptional = readerDetailsRepository.findByReaderNumber(entity.getReaderDetails().getReaderNumber());
            if (existingReaderDetailsOptional.isPresent()) {
                // Se o leitor já existe, utiliza o leitor existente
                entity.setReaderDetails(existingReaderDetailsOptional.get());
            } else {
                // Se o leitor não existe, salva o novo leitor
                ReaderDetailsEntity savedReaderDetails = readerDetailsRepository.save(entity.getReaderDetails());
                entity.setReaderDetails(savedReaderDetails);
            }
        }



        LendingEntity savedEntity = repository.save(entity); // delega o save para o JPA
        return lendingEntityMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Lending lending) {
        repository.delete(lendingEntityMapper.toEntity(lending));
    }



}