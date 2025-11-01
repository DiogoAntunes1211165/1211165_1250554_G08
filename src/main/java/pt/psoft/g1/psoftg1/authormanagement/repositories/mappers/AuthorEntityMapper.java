package pt.psoft.g1.psoftg1.authormanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.BioEntity;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.relational.NameEntity;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

@Mapper(componentModel = "spring")
public interface AuthorEntityMapper {

    // Map the entity's genId -> domain's authorNumber (domain expects the generated id string)
    @Mapping(target = "authorNumber", source = "genId")
    Author toDomain(AuthorEntity authorEntity); // convert AuthorEntity to Author


    // When converting from domain -> entity, ensure the genId field is populated from authorNumber
    @Mapping(target = "genId", source = "authorNumber")
    AuthorEntity toEntity(Author author); // convert Author to AuthorEntity

    default String map(Photo photo) {
        if (photo == null) {
            return null;
        }
        return photo.getPhotoFile();
    }

    default String map(PhotoEntity photoEntity) {
        if (photoEntity == null) {
            return null;
        }
        return photoEntity.getPhotoFile();
    }

    default String map(Name name) {
        if (name == null) {
            return null;
        }
        return name.getName();
    }

    default String map(NameEntity nameEntity) {
        if (nameEntity == null) {
            return null;
        }
        return nameEntity.getName();
    }

    default String map(Bio bio) {
        if (bio == null) {
            return null;
        }
        return bio.toString();
    }

    default String map(BioEntity bioEntity) {
        if (bioEntity == null) {
            return null;
        }
        return bioEntity.toString();
    }


}