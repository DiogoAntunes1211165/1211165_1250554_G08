package pt.psoft.g1.psoftg1.readermanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.mappers.ReaderEntityMapper;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.ReaderEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.UserEntity;
import pt.psoft.g1.psoftg1.usermanagement.repositories.mappers.UserEntityMapper;
import pt.psoft.g1.psoftg1.usermanagement.repositories.relational.UserRepositorySQL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pt.psoft.g1.psoftg1.genremanagement.repositories.relational.GenreRepositorySql;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;

@Profile("sqlServer")
@Repository("ReaderRepositorySQLImpl")
public class ReaderRepositorySQLImpl implements ReaderRepository {

    private final ReaderRepositorySQL readerRepostorySqlServer;

    private final UserRepositorySQL userRepositorySqlServer;

    private final UserEntityMapper userEntityMapper;
    private final ReaderEntityMapper readerEntityMapper;

    // Added genre repository to manage GenreEntity instances
    private final GenreRepositorySql genreRepository;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    @Lazy
    public ReaderRepositorySQLImpl(ReaderRepositorySQL readerRepostorySqlServer, UserRepositorySQL userRepositorySqlServer, UserEntityMapper userEntityMapper, ReaderEntityMapper readerEntityMapper, EntityManager em, GenreRepositorySql genreRepository) {
        this.readerRepostorySqlServer = readerRepostorySqlServer;
        this.userRepositorySqlServer = userRepositorySqlServer;
        this.userEntityMapper = userEntityMapper;
        this.readerEntityMapper = readerEntityMapper;
        this.em = em;
        this.genreRepository = genreRepository;
    }





    @Override
    @Transactional(readOnly = true)
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        if (readerRepostorySqlServer.findByReaderNumber(readerNumber).isEmpty()) {
            return Optional.empty();
        }else {
            ReaderDetailsEntity readerDetailsEntity = readerRepostorySqlServer.findByReaderNumber(readerNumber).get();
            // Ensure lazy collections are initialized while the persistence context is open
            initializeLazyCollections(readerDetailsEntity);
            return Optional.of(readerEntityMapper.toDomain(readerDetailsEntity));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : readerRepostorySqlServer.findByPhoneNumber(phoneNumber)) {
            initializeLazyCollections(readerDetail);
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }
        return readerDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReaderDetails> findByUsername(String username) {
        Optional<ReaderDetailsEntity> opt = readerRepostorySqlServer.findByUsername(username);
        if (opt.isEmpty()) return Optional.empty();
        ReaderDetailsEntity entity = opt.get();
        initializeLazyCollections(entity);
        return Optional.of(readerEntityMapper.toDomain(entity));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ReaderDetails> findByUserId(String userId) {
        Long userIdL= Long.parseLong(userId);
        Optional<ReaderDetailsEntity> opt = readerRepostorySqlServer.findByUserId(userIdL);
        if (opt.isEmpty()) return Optional.empty();
        ReaderDetailsEntity entity = opt.get();
        initializeLazyCollections(entity);
        return Optional.of(readerEntityMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public int getCountFromCurrentYear() {
        return readerRepostorySqlServer.getCountFromCurrentYear();
    }

    @Override
    @Transactional
    public ReaderDetails save(ReaderDetails readerDetails) { // save reader details
        ReaderDetailsEntity readerDetailsEntity = readerEntityMapper.toEntity(readerDetails); // convert to entity
        Optional<UserEntity> user = userRepositorySqlServer.findByUsername(readerDetails.getReader().getUsername());
        if (user.isPresent()) { // check if user exists
            System.out.println("User already exists");
            System.out.println("User: " + user.get().getUsername());
            readerDetailsEntity.setReader(userEntityMapper.toReader(user.get())); // set user to reader details

        }else {
            System.out.println("User does not exist, creating new user");
            ReaderEntity userSaved = userRepositorySqlServer.save(userEntityMapper.toEntity(readerDetails.getReader()));
            readerDetailsEntity.setReader(userSaved);
        }

        // Ensure interestList genres are managed entities to avoid TransientObjectException
        List<GenreEntity> interestList = readerDetailsEntity.getInterestList();
        if (interestList != null && !interestList.isEmpty()) {
            List<GenreEntity> managed = new ArrayList<>();
            for (GenreEntity g : interestList) {
                if (g == null) continue;
                String name = g.getGenre();
                if (name == null) continue;
                Optional<GenreEntity> existing = genreRepository.findByString(name);
                if (existing.isPresent()) {
                    managed.add(existing.get());
                } else {
                    GenreEntity saved = genreRepository.save(g);
                    managed.add(saved);
                }
            }
            readerDetailsEntity.setInterestList(managed);
        }


        return readerEntityMapper.toDomain(readerRepostorySqlServer.save(readerDetailsEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<ReaderDetails> findAll() {
        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : readerRepostorySqlServer.findAll()) {
            initializeLazyCollections(readerDetail);
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }

        return readerDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReaderDetails> findTopReaders(Pageable pageable) {

        return readerRepostorySqlServer.findTopReaders(pageable)
                .map(entity -> {
                    initializeLazyCollections(entity);
                    return readerEntityMapper.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) {
        return readerRepostorySqlServer.findTopByGenre(pageable, genre, startDate, endDate);
    }

    @Override
    public void delete(ReaderDetails readerDetails) {

    }

    @Override
    @Transactional(readOnly = true)
    public List<ReaderDetails> searchReaderDetails(final pt.psoft.g1.psoftg1.shared.services.Page page, final SearchReadersQuery query) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<ReaderDetailsEntity> cq = cb.createQuery(ReaderDetailsEntity.class);
        final Root<ReaderDetailsEntity> readerDetailsRoot = cq.from(ReaderDetailsEntity.class);
        Join<ReaderDetailsEntity, UserEntity> userJoin = readerDetailsRoot.join("reader");

        cq.select(readerDetailsRoot);

        final List<Predicate> where = new ArrayList<>();
        if (StringUtils.hasText(query.getName())) { //'contains' type search
            where.add(cb.like(userJoin.get("name").get("name"), "%" + query.getName() + "%"));
            cq.orderBy(cb.asc(userJoin.get("name")));
        }
        if (StringUtils.hasText(query.getEmail())) { //'exatct' type search
            where.add(cb.equal(userJoin.get("username"), query.getEmail()));
            cq.orderBy(cb.asc(userJoin.get("username")));

        }
        if (StringUtils.hasText(query.getPhoneNumber())) { //'exatct' type search
            where.add(cb.equal(readerDetailsRoot.get("phoneNumber").get("phoneNumber"), query.getPhoneNumber()));
            cq.orderBy(cb.asc(readerDetailsRoot.get("phoneNumber").get("phoneNumber")));
        }

        // search using OR
        if (!where.isEmpty()) {
            cq.where(cb.or(where.toArray(new Predicate[0])));
        }


        final TypedQuery<ReaderDetailsEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : q.getResultList()) {
            initializeLazyCollections(readerDetail);
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }

        return readerDetails;
    }

    /**
     * Initialize lazy collections that will be accessed by the MapStruct mapper while
     * the persistence context is still open to avoid LazyInitializationException.
     */
    private void initializeLazyCollections(ReaderDetailsEntity readerDetailsEntity) {
        if (readerDetailsEntity == null) return;
        if (readerDetailsEntity.getReader() != null) {
            try {
                // touching the collection (iterating) forces initialization while in transaction
                if (readerDetailsEntity.getReader().getAuthorities() != null) {
                    readerDetailsEntity.getReader().getAuthorities().forEach(a -> {});
                }
            } catch (Exception ignored) {
                // ignore any initialization problem here; mapper will still be called
            }
        }
        // Ensure interestList is initialized as well
        try {
            if (readerDetailsEntity.getInterestList() != null) {
                readerDetailsEntity.getInterestList().forEach(g -> {});
            }
        } catch (Exception ignored) {
            // ignore
        }
    }
}
