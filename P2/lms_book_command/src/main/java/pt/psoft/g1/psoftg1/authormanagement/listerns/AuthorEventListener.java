package pt.psoft.g1.psoftg1.authormanagement.listerns;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorView;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorViewAMQP;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;

@Component
@RequiredArgsConstructor
public class AuthorEventListener {


    @Autowired
    private AuthorService authorService;


    @RabbitListener(queues = "#{autoDeleteQueue_Author_Created.name}")
    public void receiveAuthorCreatedMessage(org.springframework.amqp.core.Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), java.nio.charset.StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            AuthorViewAMQP authorViewAMQP = mapper.readValue(jsonReceived, AuthorViewAMQP.class);

            System.out.println("Received AUTHOR_CREATED message: " + jsonReceived);

            try {
                authorService.create(authorViewAMQP);
                System.out.println("Author created successfully from AMQP message.");
            } catch (Exception e) {
                System.err.println("Error creating author from AMQP message: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error receiving author event from AMQP " + e.getMessage());
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Author_Updated.name}")
    public void receiveAuthorUpdatedMessage(org.springframework.amqp.core.Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), java.nio.charset.StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            AuthorViewAMQP authorViewAMQP = mapper.readValue(jsonReceived, AuthorViewAMQP.class);
            System.out.println("Received AUTHOR_UPDATED message: " + jsonReceived);
            try {
                authorService.update(authorViewAMQP);
                System.out.println("Author updated successfully from AMQP message.");
            } catch (Exception e) {
                System.err.println("Error updating author from AMQP message: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error receiving author event from AMQP " + e.getMessage());


        }
    }

    @RabbitListener(queues = "#{authorDeletedQueue.name}")
    public void receiveBookDeleted(String in) {
        System.out.println(" [x] Received Book Deleted '" + in + "'");
    }
}