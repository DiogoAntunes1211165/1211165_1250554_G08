package pt.psoft.g1.psoftg1.authormanagement.publishers;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewAMQP;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;



public interface AuthorEventsPublisher {

    AuthorViewAMQP sendAuthorCreated(Author author);

    AuthorViewAMQP sendAuthorUpdated(Author author, String currentVersion);

    AuthorViewAMQP sendAuthorDeleted(Author author);




}
