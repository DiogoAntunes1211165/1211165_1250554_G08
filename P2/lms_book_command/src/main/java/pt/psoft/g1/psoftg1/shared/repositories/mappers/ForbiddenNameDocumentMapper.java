package pt.psoft.g1.psoftg1.shared.repositories.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.ForbiddenNameDocument;

@Mapper(componentModel = "spring")
public interface ForbiddenNameDocumentMapper {

    ForbiddenNameDocument toDocument(ForbiddenName forbiddenName);

    ForbiddenName toModel(ForbiddenNameDocument document);
}
