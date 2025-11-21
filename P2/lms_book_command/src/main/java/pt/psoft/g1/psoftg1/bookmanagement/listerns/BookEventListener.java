package pt.psoft.g1.psoftg1.bookmanagement.listerns;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookSagaViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapper;
import pt.psoft.g1.psoftg1.bookmanagement.api.SagaCreationResponse;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisher;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BookEventListener {

    @Autowired
    private BookService bookService;

    private final BookViewAMQPMapper bookViewAMQPMapper;
    private final BookEventsPublisher bookEventsPublisher;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BookEventListener.class);




    @RabbitListener(queues = "#{autoDeleteQueue_Book_Created.name}")
    public void receiveBookCreatedMessage(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            BookViewAMQP bookViewAMQP = mapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println("Received BOOK_CREATED message: " + bookViewAMQP);

            try {
                bookService.create(bookViewAMQP);
                System.out.println("Book created successfully from AMQP message.");
            } catch (Exception e) {
                System.err.println("Error creating book from AMQP message: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error receiving book event from AMQP " + e.getMessage());
        }
    }


    @RabbitListener(queues = "#{autoDeleteQueue_Book_Updated.name}")
    public void receiveBookUpdatedMessage(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();

            BookViewAMQP bookViewAMQP = mapper.readValue(jsonReceived, BookViewAMQP.class);
            System.out.println("Received BOOK_UPDATED message: " + bookViewAMQP);

            try {
                bookService.update(bookViewAMQP);
                System.out.println("Book updated successfully from AMQP message.");
            } catch (Exception e) {
                System.err.println("Error updating book from AMQP message: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error receiving book event from AMQP " + e.getMessage());
        }
    }

    @RabbitListener(queues = "#{bookDeletedQueue.name}")
    public void receiveBookDeleted(String in) {
        System.out.println(" [x] Received Book Deleted '" + in + "'");
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Book_Lending_Requested.name}")
    public void receiveBookLendingRequestedMessage(Message msg) {

        SagaCreationResponse response = new SagaCreationResponse();

        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();

            BookSagaViewAMQP bookSagaViewAMQP = mapper.readValue(jsonReceived, BookSagaViewAMQP.class);
            logger.info("Received Book Lending Request for lending {} by AMQP: {}",
                    bookSagaViewAMQP.getLendingNumber(), bookSagaViewAMQP);

            BookViewAMQP bookViewAMQP = bookViewAMQPMapper.toBookSagaViewAMQP(bookSagaViewAMQP);

            Book book = null;

            // try to create the book
            try {
                book= bookService.create(bookViewAMQP); // cria o livro ou retorna o existente
                logger.info("Book processed successfully for lending {}: bookIsbn={}",
                        bookSagaViewAMQP.getLendingNumber(), book.getIsbn());
            } catch (Exception e) { // falha ao criar ou validar o livro
                logger.warn("Failed to create/validate book for lending {}: {}",
                        bookSagaViewAMQP.getLendingNumber(), e.getMessage());
                response.setStatus("ERROR");
                response.setLendingNumber(bookSagaViewAMQP.getLendingNumber());
                response.setError(e.getMessage());
                bookEventsPublisher.sendBookLendingResponse(response);
                return;
            }
            // Envia SUCCESS se o livro foi processado corretamente
            response.setLendingNumber(bookSagaViewAMQP.getLendingNumber());
            response.setStatus("SUCCESS");
            bookEventsPublisher.sendBookLendingResponse(response);


            // Publica evento BookCreated apenas se book não for nulo
            if (book != null) {
                bookEventsPublisher.sendBookCreated(book);
            }



        } catch (Exception e) {
            logger.error("Exception processing book lending request from AMQP", e);
        }
    }
}


