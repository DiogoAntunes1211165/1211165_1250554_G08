package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BookRabbitmqController {

    @Autowired
    private BookService bookService;

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


    @RabbitListener(queues = "#{auto<updateQueue_Book_Updated.name}")
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

}
