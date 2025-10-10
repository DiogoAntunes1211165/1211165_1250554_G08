package pt.psoft.g1.psoftg1.authormanagement.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.BioEntity;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

import java.nio.file.Path;

@Mapper(componentModel = "spring")
public interface AuthorEntityMapper {

    @Mapping(target = "authorNumber", source = "authorNumber")
    Author toDomain(AuthorEntity authorEntity);

    AuthorEntity toEntity(Author author);

    // === Photo ===
    default String map(PhotoEntity photoEntity) { // PhotoEntity to String
        return photoEntity == null ? null : photoEntity.getPhotoFile();
    }

    default PhotoEntity mapToPhotoEntity(String photo) { // String to PhotoEntity
        if (photo == null) return null;
        PhotoEntity entity = new PhotoEntity();
        entity.setPhotoFile(photo);
        return entity;
    }


    // === Bio ===
    default String map(BioEntity bioEntity) { // BioEntity to String
        return bioEntity == null ? null : bioEntity.toString();
    }

    default BioEntity mapToBioEntity(String bio) { // String to BioEntity
        if (bio == null) return null;
        BioEntity entity = new BioEntity();
        entity.setBio(bio);
        return entity;
    }

}
