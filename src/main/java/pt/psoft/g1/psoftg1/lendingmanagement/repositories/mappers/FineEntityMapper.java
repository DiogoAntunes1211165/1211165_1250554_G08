package pt.psoft.g1.psoftg1.lendingmanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.TitleEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.FineEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.relational.LendingNumberEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.relational.BirthDateEntity;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.relational.NameEntity;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

@Mapper(componentModel = "spring")
public interface FineEntityMapper {

    Fine toDomain(FineEntity fineEntity);

    FineEntity toEntity(Fine fine);

    @Mapping(target = "lendingNumber", source = "value")
    LendingNumberEntity stringToLne (String value);
    @Mapping(target = "lendingNumber", source = "value")
    LendingNumber stringToLn (String value);

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

    default String map(BirthDateEntity value) {
        if (value == null) {
            return null;
        }
        return value.toString(); // Exemplo para BirthDateEntity
    }

    default String map(BirthDate value) {
        if (value == null) {
            return null;
        }
        return value.toString(); // Exemplo para LendingNumberEntity
    }

}