package pt.psoft.g1.psoftg1.usermanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.*;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.repositories.mappers.UserDocumentMapper;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("UserRepositoryMongoDBImpl")
public class UserRepositoryMongoDBImpl implements UserRepository {

    private final UserDocumentPersistence userDocumentPersistence;
    private final UserDocumentMapper userDocumentMapper;

    @Autowired
    @Lazy
    public UserRepositoryMongoDBImpl(UserDocumentPersistence userDocumentPersistence, UserDocumentMapper userDocumentMapper) {
        this.userDocumentPersistence = userDocumentPersistence;
        this.userDocumentMapper = userDocumentMapper;
    }

    @Override
    public <S extends User> S save(S document) {
        if (document instanceof Reader) {
            ReaderDocument readerDocument = userDocumentMapper.toDocument((Reader) document);
            ReaderDocument saved = userDocumentPersistence.save(readerDocument);
            return (S) userDocumentMapper.toDomain(saved);

        } else if (document instanceof Librarian) {
            LibrarianDocument librarianDocument = userDocumentMapper.toDocument((Librarian) document);
            LibrarianDocument saved = userDocumentPersistence.save(librarianDocument);
            return (S) userDocumentMapper.toDomain(saved);

        } else if (document instanceof User) {
            UserDocument userDocument = userDocumentMapper.toDocument(document);
            UserDocument saved = userDocumentPersistence.save(userDocument);
            return (S) userDocumentMapper.toDomain(saved);
        }

        throw new IllegalArgumentException("Unsupported document type: " + document.getClass().getName());
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        List<UserDocument> docsToSave = new ArrayList<>();
        for (S entity : entities) {
            docsToSave.add(userDocumentMapper.toDocument(entity));
        }

        List<S> saved = new ArrayList<>();
        for (UserDocument doc : userDocumentPersistence.saveAll(docsToSave)) {
            saved.add((S) userDocumentMapper.toDomain(doc));
        }

        return saved;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userDocumentPersistence.findById(id)
                .map(userDocumentMapper::toDomain);
    }

    @Override
    public User getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        System.out.println("Username: " + username);
        if(userDocumentPersistence.findByUsername(username).isEmpty()){
            System.out.println("User not found");
            return Optional.empty();
        } else {
            System.out.println("User found");
            return Optional.of(userDocumentMapper.toDomain(userDocumentPersistence.findByUsername(username).get()));
        }
        /* return userDocumentPersistence.findByUsername(username)
                .map(userDocumentMapper::toModel); */
    }

    @Override
    public List<User> searchUsers(Page page, SearchUsersQuery query) {
        List<UserDocument> docs = new ArrayList<>();

        if (StringUtils.hasText(query.getUsername())) {
            userDocumentPersistence.findByUsername(query.getUsername()).ifPresent(docs::add);
        } else if (StringUtils.hasText(query.getFullName())) {
            docs.addAll(userDocumentPersistence.findByName(query.getFullName()));
        } else {
            userDocumentPersistence.findAll().forEach(docs::add);
        }

        List<User> users = new ArrayList<>();
        for (UserDocument doc : docs) {
            users.add(userDocumentMapper.toDomain(doc));
        }

        // Paginação manual
        int fromIndex = Math.max((page.getNumber() - 1) * page.getLimit(), 0);
        int toIndex = Math.min(fromIndex + page.getLimit(), users.size());

        if (fromIndex > toIndex) {
            return new ArrayList<>();
        }

        return users.subList(fromIndex, toIndex);
    }

    @Override
    public List<User> findByNameName(String name) {
        List<UserDocument> docs = userDocumentPersistence.findByName(name);
        List<User> users = new ArrayList<>();
        for (UserDocument d : docs) {
            users.add(userDocumentMapper.toDomain(d));
        }
        return users;
    }

    @Override
    public List<User> findByNameNameContains(String name) {
        // Se o repositório não suportar regex/contains, retorna vazio
        return new ArrayList<>();
    }

    @Override
    public void delete(User user) {
        UserDocument doc = userDocumentMapper.toDocument(user);
        userDocumentPersistence.delete(doc);
    }
}
