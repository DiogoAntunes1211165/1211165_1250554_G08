package pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers;



import org.mapstruct.*;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.TitleEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingNumberEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.BirthDateEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.ReaderNumberEntity;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.relational.NameEntity;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

import java.time.format.DateTimeFormatter;


@Mapper(componentModel = "spring")
public interface LendingEntityMapper {

    // Mapear de LendingEntity para Lending

    Lending toDomain(LendingEntity lendingEntity);

    // Mapear de Lending para LendingEntity

    LendingEntity toEntity(Lending lending);
    @Mapping(target = "lendingNumber", source = "value")
    LendingNumberEntity stringToLne (String value);
    @Mapping(target = "lendingNumber", source = "value")
    LendingNumber stringToLn (String value);



    @Mapping(target="birthDate", source="birthDate", qualifiedByName = "mapbde")
    @Mapping(target="gdpr", source="gdprConsent")
    @Mapping(target="marketing", source="marketingConsent")
    @Mapping(target="thirdParty", source="thirdPartySharingConsent")
    ReaderDetails toModel(ReaderDetailsEntity readerDetails);

    @Mapping(target="birthDate", source="birthDate", qualifiedByName = "mapbd")
    @Mapping(target="gdpr", source="gdprConsent")
    @Mapping(target="marketing", source="marketingConsent")
    @Mapping(target="thirdParty", source="thirdPartySharingConsent")
    ReaderDetailsEntity toEntity(ReaderDetails readerDetails);

    default int map(String value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.split("/")[1]); // Exemplo para ReaderNumberEntity
    }
    default String map(Photo value) {
        if (value == null) {
            return null;
        }
        return value.getPhotoFile(); // Exemplo para Photo
    }

    default String map(PhotoEntity value) {
        if (value == null) {
            return null;
        }
        return value.getPhotoFile(); // Exemplo para PhotoEntity
    }

    default String map(TitleEntity value) {
        if (value == null) {
            return null;
        }
        return value.getTitle(); // Exemplo para TitleEntity
    }

    default String map(Title value) {
        if (value == null) {
            return null;
        }
        return value.getTitle();  // Exemplo para Title
    }

    default String map(NameEntity value) {
        if (value == null) {
            return null;
        }
        return value.getName(); // Exemplo para NameEntity
    }

    default String map(Name value) {
        if (value == null) {
            return null;
        }
        return value.getName(); // Exemplo para LendingNumberEntity
    }

    @Named("mapbde")
    default  String map(BirthDateEntity birthDateEntity){
        if (birthDateEntity == null){
            return null;
        }
        return birthDateEntity.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Named("mapbd")
    default String map(BirthDate birthDate){
        if (birthDate == null){
            return null;
        }

        return birthDate.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}