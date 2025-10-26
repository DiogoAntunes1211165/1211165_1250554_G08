package pt.psoft.g1.psoftg1.authormanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.AuthorDocument;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.BioDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.PhotoDocument;

@Mapper(componentModel = "spring")
public interface AuthorDocumentMapper {

    @Mapping(target = "name", expression = "java(author.getName().toString())")
    @Mapping(target = "bio", expression = "java(author.getBio().toString())")
    AuthorDocument toDocument(Author author);

    @Mapping(target = "name", expression = "java(authorDocument.getName().toString())")
    @Mapping(target = "bio", expression = "java(authorDocument.getBio().toString())")
    @Mapping(target = "photo", expression = "java((authorDocument.getPhoto() != null) ? authorDocument.getPhoto().getPhotoFile() : null)")
    Author toDomain(AuthorDocument authorDocument);
}

