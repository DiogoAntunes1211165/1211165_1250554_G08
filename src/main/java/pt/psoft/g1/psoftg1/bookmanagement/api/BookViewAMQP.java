package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Schema(description = "A Book View for AMQP")
@NoArgsConstructor
public class BookViewAMQP {

    @NotNull
    private String title;

    @NotNull
    private String genre;

    @NotNull
    private String description;

    @NotNull
    private List<String> authorsIDs;

    @NotNull
    private String isbn;

    @NotNull
    private String version;

    @Getter
    @Setter
    private Map<String, Object> _links = new HashMap<>();

    public BookViewAMQP(String title, String genre, String description, List<String> authorsIDs, String isbn) {
        this.title = title;
        this.genre = genre;
        this.description = description;
        this.authorsIDs = authorsIDs;
        this.isbn = isbn;
    }

}
