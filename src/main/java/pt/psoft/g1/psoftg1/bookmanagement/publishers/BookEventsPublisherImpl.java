package pt.psoft.g1.psoftg1.bookmanagement.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;


@Service
@RequiredArgsConstructor
public class BookEventsPublisherImpl implements BookEventsPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private BookViewAMQPMapper bookViewAMQPMapper;

    private DirectExchange directExchange;


    @Override
    public BookViewAMQP sendBookCreated(Book book) {
        return sendBookEvent(book, book.getVersion(), "BOOK_CREATED");
    }

    @Override
    public BookViewAMQP sendBookUpdated(Book book, String currentVersion) {
        return sendBookEvent(book, currentVersion, "BOOK_UPDATED");

    }


    @Override
    public BookViewAMQP sendBookDeleted(Book book) {
        return sendBookEvent(book, book.getVersion(), "BOOK_DELETED");
    }

    public BookViewAMQP sendBookEvent(Book book, String currentVersion, String eventType) {
        try {
            BookViewAMQP bookViewAMQP = bookViewAMQPMapper.toBookViewAMQP(book);
            bookViewAMQP.setVersion(currentVersion);

            ObjectMapper mapper = new ObjectMapper();

            String bookViewAMQPinString = mapper.writeValueAsString(bookViewAMQP);

            this.rabbitTemplate.convertAndSend(directExchange.getName(), eventType, bookViewAMQPinString);

            return bookViewAMQP;


        } catch (JsonProcessingException e) {
            System.out.println("Error sending book event to AMQP: " + e.getMessage());
            return null;
        }
    }
}