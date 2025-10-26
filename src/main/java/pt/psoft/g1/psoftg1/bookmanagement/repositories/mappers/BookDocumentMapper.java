package pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.BioDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.BookDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.DescriptionDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.IsbnDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.TitleDocument;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.PhotoDocument;

@Mapper(componentModel = "spring")
public interface BookDocumentMapper {

    BookDocument toDocument(Book book);

    Book toDomain(BookDocument document);

    default String map(Photo value) {
        if (value == null) {
            return null;
        }
        return value.getPhotoFile();
    }

    default IsbnDocument mapToIsbnDocument(String value) {
        if (value == null) {
            return null;
        }
        return new IsbnDocument(value);
    }

    default DescriptionDocument mapToDescriptionDocument(String value) {
        if (value == null) {
            return null;
        }
        return new DescriptionDocument(value);
    }

    default String map(PhotoDocument value) {
        if (value == null) {
            return null;
        }
        return value.getPhotoFile();
    }

    default String map(NameDocument value) {
        if (value == null) {
            return null;
        }
        return value.getName();
    }

    default String map(BioDocument value) {
        if (value == null) {
            return null;
        }
        return value.getBio();
    }

    default String map(IsbnDocument value) {
        if (value == null) {
            return null;
        }
        return value.getIsbn();
    }

    default String map(TitleDocument value) {
        if (value == null) {
            return null;
        }
        return value.getTitle();
    }

    default String map(DescriptionDocument value) {
        if (value == null) {
            return null;
        }
        return value.getDescription();
    }
}
