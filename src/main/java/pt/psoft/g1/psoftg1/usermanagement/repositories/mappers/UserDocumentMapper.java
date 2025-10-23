package pt.psoft.g1.psoftg1.usermanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.LibrarianDocument;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.ReaderDocument;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.UserDocument;

@Mapper(componentModel = "spring")
public interface UserDocumentMapper {

    User toModel(UserDocument document);

    Librarian toModel(LibrarianDocument document);

    Reader toModel(ReaderDocument document);

    ReaderDocument toDocument(Reader user);

    LibrarianDocument toDocument(Librarian user);

    UserDocument toDocument(User user);

    @Mapping(target = "id", expression = "java(document.getId())")
    @Mapping(target = "enabled", expression = "java(true)")
    ReaderDocument toReader(UserDocument document);

    default String map(NameDocument nameDocument) {
        return nameDocument != null ? nameDocument.toString() : null;
    }

    default String map(Name name) {
        return name != null ? name.toString() : null;
    }
}
