package pt.psoft.g1.psoftg1.readermanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.BirthDateDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderDetailsDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderNumberDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.PhoneNumberDocument;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.PhotoDocument;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ReaderDocumentMapper {

    @Mapping(source = "readerNumber", target = "readerNumber")
    @Mapping(source = "reader", target = "reader")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "gdprConsent", target = "gdpr")
    @Mapping(source = "marketingConsent", target = "marketing")
    @Mapping(source = "thirdPartySharingConsent", target = "thirdParty")
    @Mapping(source = "photo", target = "photoURI")
    @Mapping(source = "interestList", target = "interestList")
    ReaderDetails toDomain(ReaderDetailsDocument readerDetails);

    ReaderDetailsDocument toDocument(ReaderDetails readerDetails);

    default String map(PhotoDocument value) {
        return value == null ? null : value.getPhotoFile();
    }

    default String map(NameDocument value) {
        return value == null ? null : value.getName();
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
        return value.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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

    default String map(Name value) {
        if (value == null) {
            return null;
        }
        return value.getName();
    }

    default ReaderNumberDocument map(String value) {
        if (value == null) {
            return null;
        }
        return new ReaderNumberDocument(Integer.parseInt(value.split("/")[1]));
    }

    default BirthDateDocument map(BirthDate value) {
        if (value == null || value.getBirthDate() == null) {
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

}
