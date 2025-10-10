package pt.psoft.g1.psoftg1.authormanagement.repositories.relational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.mappers.AuthorEntityMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Profile("sqlServer")
@Qualifier("AuthorRepositorySqlImpl")
@Component
public class AuthorRepositorySqlImpl implements AuthorRepository {

    private AuthorEntityMapper authorEntityMapper;
    private AuthorRepositorySQL authorRepositorySQL;

    @Autowired
    @Lazy
    public AuthorRepositorySqlImpl(AuthorEntityMapper authorEntityMapper, AuthorRepositorySQL authorRepositorySQL) {
        this.authorEntityMapper = authorEntityMapper;
        this.authorRepositorySQL = authorRepositorySQL;
    }

    @Override
    public Optional<Author> findByAuthorNumber(Long authorNumber) {
        if (authorRepositorySQL.findByAuthorNumber(authorNumber).isEmpty()) {
            return Optional.empty();
        } else {
            Author author = authorEntityMapper.toDomain(authorRepositorySQL.findByAuthorNumber(authorNumber).get());
            return Optional.of(author);
        }


    }

    @Override
    public List<Author> searchByNameNameStartsWith(String name) {
        List<Author> authors = new ArrayList<>();

        authorRepositorySQL.findByNameStartsWith(name).forEach(authorEntity -> authors.add(authorEntityMapper.toDomain(authorEntity))); // Convert each AuthorEntity to Author and add to the list
        return authors;
    }

    @Override
    public List<Author> searchByNameName(String name) {
        List<Author> authors = new ArrayList<>();

        authorRepositorySQL.searchByNameName(name).forEach(authorEntity -> authors.add(authorEntityMapper.toDomain(authorEntity))); // Convert each AuthorEntity to Author and add to the list

        return authors;
    }

    @Override
    public Author save(Author author) {
        return authorEntityMapper.toDomain(authorRepositorySQL.save(authorEntityMapper.toEntity(author))); // Convert Author to AuthorEntity, save it, and convert back to Author
    }

    @Override
    public Iterable<Author> findAll() {
        return null;
    }

    @Override
    public Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageableRules) {
        return authorRepositorySQL.findTopAuthorByLendings(pageableRules);
    }

    @Override
    public void delete(Author author) {
        authorRepositorySQL.delete(authorEntityMapper.toEntity(author)); // Convert Author to AuthorEntity and delete it

    }

    @Override
    public List<Author> findCoAuthorsByAuthorNumber(Long authorNumber) {
        return List.of();
    }
}
