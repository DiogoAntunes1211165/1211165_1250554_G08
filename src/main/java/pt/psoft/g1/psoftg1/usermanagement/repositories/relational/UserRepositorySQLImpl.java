package pt.psoft.g1.psoftg1.usermanagement.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.*;
import pt.psoft.g1.psoftg1.usermanagement.model.relational.*;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.repositories.mappers.UserEntityMapper;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sqlServer")
@Repository("UserRepositorySQLImpl")
public class UserRepositorySQLImpl implements UserRepository {

    private final UserRepositorySQL userRepositorySql;
    private final UserEntityMapper userEntityMapper;

    @Autowired
    @Lazy
    public UserRepositorySQLImpl(UserRepositorySQL userRepositorySql, UserEntityMapper userEntityMapper) {
        this.userRepositorySql = userRepositorySql;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public <S extends User> S save(S entity) {

        if (entity instanceof Reader) {
            ReaderEntity readerEntity = userEntityMapper.toEntity((Reader) entity);
            ReaderEntity savedEntity = userRepositorySql.save(readerEntity);
            return (S) userEntityMapper.toModel(savedEntity);

        } else if (entity instanceof Librarian) {
            LibrarianEntity librarianEntity = userEntityMapper.toEntity((Librarian) entity);
            LibrarianEntity savedEntity = userRepositorySql.save(librarianEntity);
            return (S) userEntityMapper.toModel(savedEntity);

        } else if (entity instanceof User) {
            UserEntity userEntity = userEntityMapper.toEntity(entity);
            UserEntity savedEntity = userRepositorySql.save(userEntity);
            return (S) userEntityMapper.toModel(savedEntity);
        }

        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getName());
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {

        List<S> savedEntities = new ArrayList<>();

        List<UserEntity> userEntitiesToSave = new ArrayList<>();

        for (S entity : entities) {
            userEntitiesToSave.add(userEntityMapper.toEntity(entity));
        }

        for (UserEntity userEntity : userRepositorySql.saveAll(userEntitiesToSave)) {
            savedEntities.add((S) userEntityMapper.toModel(userEntity));
        }


        return savedEntities;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepositorySql.findById(id)
                .map(userEntityMapper::toModel);
    }

    @Override
    public User getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepositorySql.findByUsername(username)
                .map(userEntityMapper::toModel);
    }

    @Override
    public List<User> searchUsers(Page page, SearchUsersQuery query) {
        List<UserEntity> userEntities = new ArrayList<>();

        if (StringUtils.hasText(query.getUsername())) {
            userRepositorySql.findByUsername(query.getUsername()).ifPresent(userEntities::add);
        } else if (StringUtils.hasText(query.getFullName())) {
            userEntities.addAll(userRepositorySql.findByName(query.getFullName()));
        } else {
            userRepositorySql.findAll().forEach(userEntities::add);
        }

        List<User> users = new ArrayList<>();
        for (UserEntity u : userEntities) {
            users.add(userEntityMapper.toModel(u));
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
        List<UserEntity> entities = userRepositorySql.findByName(name);
        List<User> users = new ArrayList<>();
        for (UserEntity u : entities) {
            users.add(userEntityMapper.toModel(u));
        }
        return users;
    }

    @Override
    public List<User> findByNameNameContains(String name) {
        // Não temos suporte direto a contains no SQL nativo do repo, então apenas retorna vazio
        return new ArrayList<>();
    }

    @Override
    public void delete(User user) {
        UserEntity entityToDelete = userEntityMapper.toEntity(user);
        userRepositorySql.delete(entityToDelete);
    }
}
