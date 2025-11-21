package pt.psoft.g1.psoftg1.bookmanagement.publishers;

import pt.psoft.g1.psoftg1.bookmanagement.api.BookView;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.SagaCreationResponse;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

public interface BookEventsPublisher {

    BookViewAMQP sendBookCreated(Book book);

    BookViewAMQP sendBookUpdated(Book book, String currentVersion);

    BookViewAMQP sendBookDeleted(Book book);

    void sendBookLendingResponse(SagaCreationResponse response);
}
