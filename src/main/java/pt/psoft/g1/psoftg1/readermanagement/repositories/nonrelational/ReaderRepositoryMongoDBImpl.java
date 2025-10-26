package pt.psoft.g1.psoftg1.readermanagement.repositories.nonrelational;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderDetailsDocument;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.mappers.ReaderDocumentMapper;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.repositories.nonrelational.UserDocumentPersistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("mongodb")
@Repository("ReaderRepositoryMongoDBImpl")
public class ReaderRepositoryMongoDBImpl implements ReaderRepository {

    private final ReaderDocumentPersistence readerDocumentPersistence;
    private final UserDocumentPersistence userDocumentPersistence;
    private final ReaderDocumentMapper readerDocumentMapper;

    @Autowired
    @Lazy
    public ReaderRepositoryMongoDBImpl(
            ReaderDocumentPersistence readerDocumentPersistence,
            UserDocumentPersistence userDocumentPersistence,
            ReaderDocumentMapper readerDocumentMapper
    ) {
        this.readerDocumentPersistence = readerDocumentPersistence;
        this.userDocumentPersistence = userDocumentPersistence;
        this.readerDocumentMapper = readerDocumentMapper;
    }

    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        return readerDocumentPersistence.findByReaderNumber(readerNumber)
                .map(readerDocumentMapper::toDomain);
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        List<ReaderDetailsDocument> docs = readerDocumentPersistence.findByPhoneNumber(phoneNumber);
        List<ReaderDetails> readers = new ArrayList<>();
        for (ReaderDetailsDocument doc : docs) {
            readers.add(readerDocumentMapper.toDomain(doc));
        }
        return readers;
    }

    @Override
    public Optional<ReaderDetails> findByUsername(String username) {
        return readerDocumentPersistence.findByReader_Username(username)
                .map(readerDocumentMapper::toDomain);
    }

    @Override
    public Optional<ReaderDetails> findByUserId(String userId) {
        return readerDocumentPersistence.findByUserId(userId)
                .map(readerDocumentMapper::toDomain);
    }

    @Override
    public int getCountFromCurrentYear() {
        return readerDocumentPersistence.getCountFromCurrentYear();
    }

    @Override
    public ReaderDetails save(ReaderDetails readerDetails) {
        try {
            ReaderDetailsDocument doc = readerDocumentMapper.toDocument(readerDetails);
            // Ensure the referenced ReaderDocument (used as @DBRef) has a non-null id.
            if (doc.getReader() != null && doc.getReader().getId() == null) {
                String username = doc.getReader().getUsername();
                if (username != null) {
                    Optional<pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.UserDocument> existing = userDocumentPersistence.findByUsername(username);
                    if (existing.isPresent()) {
                        // reuse existing user document (will have an id)
                        doc.setReader((pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.ReaderDocument) existing.get());
                    } else {
                        // persist the reader document into Mongo to obtain an id
                        pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.UserDocument savedUser = userDocumentPersistence.save(doc.getReader());
                        doc.setReader((pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.ReaderDocument) savedUser);
                    }
                }
            }
            ReaderDetailsDocument savedDoc = readerDocumentPersistence.insert(doc);
            return readerDocumentMapper.toDomain(savedDoc);
        } catch (Exception e) {
            e.printStackTrace(); // mostra qualquer exceção antes do println
            throw e;
        }
    }


    @Override
    public Iterable<ReaderDetails> findAll() {
        List<ReaderDetails> list = new ArrayList<>();
        for (ReaderDetailsDocument doc : readerDocumentPersistence.findAll()) {
            list.add(readerDocumentMapper.toDomain(doc));
        }
        return list;
    }

    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable) {
        /* Page<ReaderDetailsDocument> docs = readerDocumentPersistence.findTopReaders(pageable);
        return docs.map(readerDocumentMapper::toDomain); */
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Method not implemented yet"); // return readerDocumentPersistence.findTopByGenre(pageable, genre, startDate, endDate);
    }

    @Override
    public void delete(ReaderDetails readerDetails) {
        readerDocumentPersistence.findByReaderNumber(readerDetails.getReaderNumber())
                .ifPresent(readerDocumentPersistence::delete);
    }

    @Override
    public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
        String name = query.getName();
        String email = query.getEmail();
        String phoneNumber = query.getPhoneNumber();

        List<ReaderDetails> results = new ArrayList<>();

        // Pesquisa por nome do utilizador
        if (name != null && !name.isBlank()) {
            readerDocumentPersistence.findByReader_Username(name)
                    .ifPresent(doc -> results.add(readerDocumentMapper.toDomain(doc)));
        }

        // Pesquisa por email
        if (email != null && !email.isBlank()) {
            readerDocumentPersistence.findByReader_Email(email)
                    .ifPresent(doc -> results.add(readerDocumentMapper.toDomain(doc)));
        }

        // Pesquisa por número de telefone
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            List<ReaderDetailsDocument> phoneResults = readerDocumentPersistence.findByPhoneNumber(phoneNumber);
            for (ReaderDetailsDocument doc : phoneResults) {
                results.add(readerDocumentMapper.toDomain(doc));
            }
        }

        // Paginação manual simples
        int start = (page.getNumber() - 1) * page.getLimit();
        int end = Math.min(start + page.getLimit(), results.size());
        if (start > results.size()) return List.of();

        return results.subList(start, end);
    }


}
