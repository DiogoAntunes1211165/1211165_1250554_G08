package pt.psoft.g1.psoftg1.readermanagement.repositories.relational;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.mappers.ReaderEntityMapper;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.ReaderEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.UserEntity;
import pt.psoft.g1.psoftg1.usermanagement.repositories.mappers.UserEntityMapper;
import pt.psoft.g1.psoftg1.usermanagement.repositories.relational.UserRepositorySQL;

import pt.psoft.g1.psoftg1.usermanagement.repositories.relational.UserRepositorySQL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sqlServer")
@Repository("ReaderRepositorySQLImpl")
public class ReaderRepositorySQLImpl implements ReaderRepository {

    private final ReaderRepositorySQL readerRepostorySqlServer;

    private final UserRepositorySQL userRepositorySqlServer;

    private final UserEntityMapper userEntityMapper;
    private final ReaderEntityMapper readerEntityMapper;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    @Lazy
    public ReaderRepositorySQLImpl(ReaderRepositorySQL readerRepostorySqlServer, UserRepositorySQL userRepositorySqlServer, UserEntityMapper userEntityMapper, ReaderEntityMapper readerEntityMapper, EntityManager em) {
        this.readerRepostorySqlServer = readerRepostorySqlServer;
        this.userRepositorySqlServer = userRepositorySqlServer;
        this.userEntityMapper = userEntityMapper;
        this.readerEntityMapper = readerEntityMapper;
        this.em = em;
    }





    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        if (readerRepostorySqlServer.findByReaderNumber(readerNumber).isEmpty()) {
            return Optional.empty();
        }else {
            ReaderDetailsEntity readerDetailsEntity = readerRepostorySqlServer.findByReaderNumber(readerNumber).get();
            //System.out.println(readerDetailsEntity.getReader());
            return Optional.of(readerEntityMapper.toDomain(readerDetailsEntity));
        }
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : readerRepostorySqlServer.findByPhoneNumber(phoneNumber)) {
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }
        return readerDetails;
    }

    @Override
    public Optional<ReaderDetails> findByUsername(String username) {
        return Optional.of(readerEntityMapper.toDomain(readerRepostorySqlServer.findByUsername(username).get()));
    }


    @Override
    public Optional<ReaderDetails> findByUserId(String userId) {
        Long userIdL= Long.parseLong(userId);
        return Optional.of(readerEntityMapper.toDomain(readerRepostorySqlServer.findByUserId(userIdL).get()));
    }

    @Override
    public int getCountFromCurrentYear() {
        return readerRepostorySqlServer.getCountFromCurrentYear();
    }

    @Override
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


        return readerEntityMapper.toDomain(readerRepostorySqlServer.save(readerDetailsEntity));
    }

    @Override
    public Iterable<ReaderDetails> findAll() {
        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : readerRepostorySqlServer.findAll()) {
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }

        return readerDetails;
    }

    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable) {

        return readerRepostorySqlServer.findTopReaders(pageable).map(readerEntityMapper::toDomain);
    }

    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) {
        return readerRepostorySqlServer.findTopByGenre(pageable, genre, startDate, endDate);
    }

    @Override
    public void delete(ReaderDetails readerDetails) {

    }

    @Override
    public List<ReaderDetails> searchReaderDetails(final pt.psoft.g1.psoftg1.shared.services.Page page, final SearchReadersQuery query) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<ReaderDetailsEntity> cq = cb.createQuery(ReaderDetailsEntity.class);
        final Root<ReaderDetailsEntity> readerDetailsRoot = cq.from(ReaderDetailsEntity.class);
        Join<ReaderDetailsEntity, User> userJoin = readerDetailsRoot.join("reader");

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
            readerDetails.add(readerEntityMapper.toDomain(readerDetail));
        }

        return readerDetails;
    }
}