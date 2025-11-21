package pt.psoft.g1.psoftg1.genremanagement.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;

@Mapper(componentModel = "spring")

public interface GenreDocumentMapper {

    GenreDocument toDocument(Genre genre);

    Genre toDomain(GenreDocument genreDocument);
}
