package pt.psoft.g1.psoftg1.authormanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.mappers.AuthorDocumentMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("AuthorRepositoryMongoDBImpl")
public class AuthorRepositoryMongoDBImpl implements AuthorRepository{

    private AuthorRepositoryMongoDB authorRepositoryMongoDB;
    private AuthorDocumentMapper authorDocumentMapper;

    @Autowired
    @Lazy
    public AuthorRepositoryMongoDBImpl(AuthorRepositoryMongoDB authorRepositoryMongoDB, AuthorDocumentMapper authorDocumentMapper) {
        this.authorRepositoryMongoDB = authorRepositoryMongoDB;
        this.authorDocumentMapper = authorDocumentMapper;
    }

    @Override
    public Optional<Author> findByAuthorNumber(String authorNumber) {
        return authorRepositoryMongoDB.findByAuthorNumber(authorNumber)
                .map(authorDocumentMapper::toDomain);
    }

    @Override
    public List<Author> searchByNameNameStartsWith(String name) {
        var documents = authorRepositoryMongoDB.findByNameStartsWith(name);
        var authors = new ArrayList<Author>();
        documents.forEach(doc -> authors.add(authorDocumentMapper.toDomain(doc)));
        return authors;
    }

    @Override
    public List<Author> searchByNameName(String name) {
        var documents = authorRepositoryMongoDB.findByName(name);
        var authors = new ArrayList<Author>();
        documents.forEach(doc -> authors.add(authorDocumentMapper.toDomain(doc)));
        return authors;
    }

    @Override
    public Author save(Author author) {
        var saved = authorRepositoryMongoDB.insert(authorDocumentMapper.toDocument(author));
        return authorDocumentMapper.toDomain(saved);
    }

    @Override
    public Iterable<Author> findAll() {
        var result = new ArrayList<Author>();
        authorRepositoryMongoDB.findAll().forEach(doc -> result.add(authorDocumentMapper.toDomain(doc)));
        return result;
    }

    @Override
    public Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageableRules) {
        // MongoDB version might not support this directly; implement if needed
        throw new UnsupportedOperationException("Not implemented for MongoDB");
    }

    @Override
    public void delete(Author author) {
        authorRepositoryMongoDB.delete(authorDocumentMapper.toDocument(author));
    }

    @Override
    public List<Author> findCoAuthorsByAuthorNumber(String authorNumber) {
        return List.of(); // optional feature
    }
}
