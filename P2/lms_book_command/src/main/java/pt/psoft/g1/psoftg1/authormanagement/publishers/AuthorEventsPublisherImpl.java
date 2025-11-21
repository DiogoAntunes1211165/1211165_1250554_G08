package pt.psoft.g1.psoftg1.authormanagement.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewAMQP;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewAMQPMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;

import java.io.Serial;

@Service
@RequiredArgsConstructor
public class AuthorEventsPublisherImpl implements AuthorEventsPublisher {

    @Autowired
    private RabbitTemplate template;
    @Autowired
    private DirectExchange direct;
    @Autowired
    private AuthorViewAMQPMapper authorViewAMQPMapper;


    @Override
    public AuthorViewAMQP sendAuthorCreated(Author author) {
        return sendAuthorEvent(author, author.getVersion().toString(), "AUTHOR_CREATED");
    }

    @Override
    public AuthorViewAMQP sendAuthorUpdated(Author author, String currentVersion) {
        return sendAuthorEvent(author, currentVersion, "AUTHOR_UPDATED");
    }

    @Override
    public AuthorViewAMQP sendAuthorDeleted(Author author) {
        return sendAuthorEvent(author, author.getVersion().toString(), "AUTHOR_DELETED");
    }


    public AuthorViewAMQP sendAuthorEvent(Author author, String currentVersion, String authorEventType) {

        try {
            ObjectMapper objectMapper = new ObjectMapper(); // Create ObjectMapper instance

            AuthorViewAMQP authorViewAMQP = authorViewAMQPMapper.toAuthorViewAMQP(author); // Convert Author to AuthorViewAMQP
            authorViewAMQP.setVersion(currentVersion); // Set the version

            String jsonString = objectMapper.writeValueAsString(authorViewAMQP); // Serialize AuthorViewAMQP to JSON string

            this.template.convertAndSend(direct.getName(), authorEventType, jsonString); // Send the message to RabbitMQ

            System.out.println(" [x] Sent author event: '" + jsonString + "'");

            return authorViewAMQP;


        } catch (Exception ex) {
            System.out.println(" [x] Exception sending author event: '" + ex.getMessage() + "'");
            return null;
        }

    }
}
