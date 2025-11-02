package pt.psoft.g1.psoftg1.lendingmanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.relational.BookRepositorySQL;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.relational.ReaderRepositorySQL;

import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers.LendingEntityMapper;

import pt.psoft.g1.psoftg1.genremanagement.repositories.relational.GenreRepositorySql;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingNumberEntity;

@Profile("sqlServer")
@Repository("LendingRepositorySQLImpl")
public class LendingRepositorySQLImpl implements LendingRepository {

    private static final Logger logger = LoggerFactory.getLogger(LendingRepositorySQLImpl.class);

    private final LendingRepositorySQL repository;
    private final LendingEntityMapper lendingEntityMapper;
    private final ReaderRepositorySQL readerDetailsRepository;
    private final BookRepositorySQL bookRepository;
    private final GenreRepositorySql genreRepository;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    @Lazy
    public LendingRepositorySQLImpl(LendingRepositorySQL repository, LendingEntityMapper lendingEntityMapper, ReaderRepositorySQL readerDetailsRepository, BookRepositorySQL bookRepository, GenreRepositorySql genreRepository, EntityManager em) {
        this.repository = repository;
        this.lendingEntityMapper = lendingEntityMapper;
        this.readerDetailsRepository = readerDetailsRepository;
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.em = em;
    }



    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<Lending> getOverdue(Page page) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        final Root<LendingEntity> root = cq.from(LendingEntity.class);
        cq.select(root);
        cq.distinct(true);

        final List<Predicate> where = new ArrayList<>();

        // ensure we fetch book and its authors and readerDetails to avoid later lazy access in the mapper
        root.fetch("book", JoinType.LEFT).fetch("authors", JoinType.LEFT);
        root.fetch("readerDetails", JoinType.LEFT);

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
    @Transactional(readOnly = true)
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        final Root<LendingEntity> lendingRoot = cq.from(LendingEntity.class);
        // Use entity types for joins to match JPA metamodel and avoid ClassCastException
        final Join<LendingEntity, BookEntity> bookJoin = lendingRoot.join("book");
        final Join<LendingEntity, ReaderDetailsEntity> readerDetailsJoin = lendingRoot.join("readerDetails");
        // make sure authors are fetched to avoid lazy initialization when mapping
        lendingRoot.fetch("book", JoinType.LEFT).fetch("authors", JoinType.LEFT);
        cq.select(lendingRoot);
        cq.distinct(true);

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
    @Transactional
    public Lending save(Lending lending) {
        LendingEntity entity = lendingEntityMapper.toEntity(lending);

        int attempts = 0;
        while (true) {
            try {
                // Book handling: try to reuse existing book or save new
                if (entity.getBook() != null && entity.getBook().getIsbn() != null) {
                    Optional<BookEntity> existingBookOptional = bookRepository.findByIsbn(entity.getBook().getIsbn());
                    if (existingBookOptional.isPresent()) {
                        entity.setBook(existingBookOptional.get());
                    } else {
                        BookEntity bookToSave = entity.getBook();
                        if (bookToSave.getGenre() != null) {
                            String genreName = bookToSave.getGenre().getGenre();
                            Optional<GenreEntity> existingGenreOpt = genreRepository.findByString(genreName);
                            if (existingGenreOpt.isPresent()) {
                                bookToSave.setGenre(existingGenreOpt.get());
                            } else {
                                GenreEntity savedGenre = genreRepository.save(bookToSave.getGenre());
                                // Ensure the genre is flushed to the DB before saving the book that references it
                                em.flush();
                                bookToSave.setGenre(savedGenre);
                            }
                        }
                        BookEntity savedBook = bookRepository.save(bookToSave);
                        // flush so any constraint issues surface here
                        em.flush();
                        entity.setBook(savedBook);
                    }
                }

                // Reader details handling
                if(entity.getReaderDetails() != null && entity.getReaderDetails().getReaderNumber() != null){
                    Optional<ReaderDetailsEntity> existingReaderDetailsOptional = readerDetailsRepository.findByReaderNumber(entity.getReaderDetails().getReaderNumber());
                    if (existingReaderDetailsOptional.isPresent()) {
                        entity.setReaderDetails(existingReaderDetailsOptional.get());
                    } else {
                        ReaderDetailsEntity savedReaderDetails = readerDetailsRepository.save(entity.getReaderDetails());
                        entity.setReaderDetails(savedReaderDetails);
                    }
                }

                // ---- NEW FLOW: generate lendingNumber as {year}/{seq} where seq = countFromCurrentYear+1 ----
                // If caller provided a lending number, keep it; otherwise compute the next seq for the year
                LendingNumberEntity provided = entity.getLendingNumberEntity();
                if (provided == null) {
                    int nextSeq = this.repository.getCountFromCurrentYear() + 1;
                    // generate as {year}/{nextSeq} (LendingNumberEntity(int) uses current year)
                    entity.setLendingNumberEntity(new LendingNumberEntity(nextSeq));
                } else {
                    // keep provided lending number
                    entity.setLendingNumberEntity(provided);
                }

                // First save: try to persist with the generated lending number. If a concurrent transaction already
                // used the same number, the catch block will regenerate and retry several times.
                LendingEntity savedEntity = repository.save(entity);
                // force flush so DB assigns values and constraint violations surface now
                em.flush();

                return lendingEntityMapper.toDomain(savedEntity);
            } catch (DataIntegrityViolationException | PersistenceException ex) {
                attempts++;
                // Allow several attempts to recover (concurrent creates can cause lending-number collisions).
                if (attempts > 5) {
                    throw ex; // give up after several attempts
                }

                // First, try to regenerate a fresh lending number sequence to avoid UNIQUE constraint on LENDING_NUMBER
                try {
                    int newSeq = this.repository.getCountFromCurrentYear() + 1;
                    entity.setLendingNumberEntity(new LendingNumberEntity(newSeq));
                    logger.warn("Lending save conflict detected - regenerated lending number to {} (attempt {})", newSeq, attempts);
                } catch (Exception regenEx) {
                    logger.warn("Failed to regenerate lending number during recovery attempt {}", attempts, regenEx);
                    // ignore regeneration errors and fall back to other recovery steps
                }

                // Then try to recover by re-querying the book by ISBN using EntityManager (bypass caches)
                try {
                    if (entity.getBook() != null && entity.getBook().getIsbn() != null) {
                        TypedQuery<BookEntity> tq = em.createQuery("SELECT b FROM BookEntity b WHERE b.isbn.isbn = :isbn", BookEntity.class);
                        tq.setParameter("isbn", entity.getBook().getIsbn());
                        BookEntity found = tq.getSingleResult();
                        entity.setBook(found);
                    }
                } catch (NoResultException nre) {
                    // If we can't find it, rethrow original exception to surface error
                    logger.error("Failed to recover BookEntity by ISBN while recovering from DataIntegrityViolationException", nre);
                    throw ex;
                }
                // and retry the loop with the (possibly) new lending number
             }
         }
     }


     @Override
     @Transactional
     public void delete(Lending lending) {
         repository.delete(lendingEntityMapper.toEntity(lending));
     }



}
