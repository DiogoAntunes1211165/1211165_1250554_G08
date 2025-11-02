package pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import pt.psoft.g1.psoftg1.authormanagement.model.nonrelational.BioDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.DescriptionDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.IsbnDocument;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.TitleDocument;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.LendingDocument;
import pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational.LendingNumberDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.BirthDateDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.PhoneNumberDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderNumberDocument;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.PhotoDocument;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface LendingDocumentMapper {

    @Mapping(source = "lendingNumber", target = "lendingNumber")
    LendingDocument toDocument(Lending lending);

    @Mapping(source = "readerDetails.gdprConsent", target = "readerDetails.gdpr")
    @Mapping(source = "readerDetails.marketingConsent", target = "readerDetails.marketing")
    @Mapping(source = "readerDetails.thirdPartySharingConsent", target = "readerDetails.thirdParty")
    @Mapping(source = "readerDetails.photo", target = "readerDetails.photoURI")
    @Mapping(source = "lendingNumber", target = "lendingNumber")
    Lending toDomain(LendingDocument document);

    default LendingNumberDocument map(String value){
        if (value == null){
            return null;
        }
        return new LendingNumberDocument(value);
    }

    default String map(PhotoDocument photoDocument) {
        if (photoDocument == null) {
            return null;
        }
        return photoDocument.getPhotoFile();
    }

    default String map(NameDocument value) {
        if (value == null) {
            return null;
        }
        return value.getName();
    }

    default int map(ReaderNumberDocument value) {
        if (value == null) {
            return 0;
        }
        return value.getReaderNumber();
    }

    default String map(BirthDateDocument value) {
        if (value == null) {
            return null;
        }
        return value.getBirthDate().toString();
    }

    default String map(PhoneNumberDocument value) {
        if (value == null) {
            return null;
        }
        return value.getPhoneNumber();
    }

    default String map(Photo value) {
        if (value == null) {
            return null;
        }
        return value.getPhotoFile();
    }

    // renomeados para evitar colisão com outros map(String)
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

    default String map(Name value) {
        if (value == null) {
            return null;
        }
        return value.getName();
    }

    default ReaderNumberDocument mapToReaderNumberDocument(String value) {
        if (value == null) {
            return null;
        }
        return new ReaderNumberDocument(Integer.parseInt(value.split("/")[1]));
    }

    default BirthDateDocument map(BirthDate value) {
        if (value == null) {
            return null;
        }
        return new BirthDateDocument(value.getBirthDate().toString());
    }

    default PhoneNumberDocument mapToPhoneNumberDocument(String value) {
        if (value == null) {
            return null;
        }
        return new PhoneNumberDocument(value);
    }

    default Integer map(Optional<Integer> value) {
        return value.orElse(null);
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
