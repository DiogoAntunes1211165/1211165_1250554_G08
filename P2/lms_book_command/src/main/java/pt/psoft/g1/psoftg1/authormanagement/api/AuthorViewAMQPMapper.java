package pt.psoft.g1.psoftg1.authormanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;


@Mapper(componentModel = "spring")

public abstract class AuthorViewAMQPMapper extends MapperInterface {


    @Mapping(target = "version", expression = "java(author.getVersion().toString())")
    public abstract AuthorViewAMQP toAuthorViewAMQP(Author author);





}



