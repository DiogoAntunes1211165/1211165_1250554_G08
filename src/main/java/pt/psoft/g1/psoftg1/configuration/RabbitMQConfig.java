package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.books");
    }

    @Bean(name = "autoDeleteQueue_Book_Created")
    public Queue autoDeleteQueue_Book_Created() { //
        System.out.println("autoDeleteQueue_Book_Created bean created");
        return new AnonymousQueue(); // Cria uma fila anônima que será apagada automaticamente
    }

    @Bean(name = "autoDeleteQueue_Book_Deleted")
    public Queue autoDeleteQueue_Book_Deleted() {
        System.out.println("autoDeleteQueue_Book_Deleted bean created");
        return new AnonymousQueue(); // Cria uma fila anônima que será apagada automaticamente
    }

    @Bean(name = "autoDeleteQueue_Book_Updated")
    public Queue autoDeleteQueue_Book_Updated() {
        System.out.println("autoDeleteQueue_Book_Updated bean created");
        return new AnonymousQueue(); // Cria uma fila anônima que será apagada automaticamente
    }


    @Bean
    public Binding binding1(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Created)
                .to(direct)
                .with(BookEvents.BOOK_CREATED);
    }

    @Bean
    public Binding binding2(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Deleted") Queue autoDeleteQueue_Book_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Deleted)
                .to(direct)
                .with(BookEvents.BOOK_DELETED);
    }

    @Bean
    public Binding binding3(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Updated") Queue autoDeleteQueue_Book_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Updated)
                .to(direct)
                .with(BookEvents.BOOK_UPDATED);


    }
}
