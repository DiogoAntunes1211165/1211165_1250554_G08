package pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.DescriptionDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.IsbnDocument;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.FineDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.BirthDateDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.PhoneNumberDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderNumberDocument;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface FineDocumentMapper {

    FineDocument toDocument(Fine fine);

    Fine toDomain(FineDocument fineDocument);

    default String map(Photo value){
        return value != null ? value.getPhotoFile() : null;
    }
    default IsbnDocument mapToIsbnDocument(String value){
        return value != null ? new IsbnDocument(value) : null;
    }
    default DescriptionDocument mapToDescriptionDocument(String value){
        return value != null ? new DescriptionDocument(value) : null;
    }
    default String map(Name value){
        return value != null ? value.getName() : null;
    }
    default ReaderNumberDocument mapToReaderNumberDocument(String value){
        return value != null ? new ReaderNumberDocument(Integer.parseInt(value.split("/")[1])) : null;
    }
    default BirthDateDocument map(BirthDate value){
        return value != null ? new BirthDateDocument(value.getBirthDate().toString()) : null;
    }
    default PhoneNumberDocument map(String value){
        return value != null ? new PhoneNumberDocument(value) : null;
    }
    default Integer map(Optional<Integer> value){
        return value.orElse(null);
    }

}
