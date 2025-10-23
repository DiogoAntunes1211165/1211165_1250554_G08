package pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.BookDocument;

@Mapper(componentModel = "spring")
public interface BookDocumentMapper {

    @Mapping(target = "isbn", source = "book.getIsbn()")
    @Mapping(target = "photo", source = "book.getPhoto().getPhotoFile()")
    BookDocument toDocument(Book book);

    Book toDomain(BookDocument document);
}
