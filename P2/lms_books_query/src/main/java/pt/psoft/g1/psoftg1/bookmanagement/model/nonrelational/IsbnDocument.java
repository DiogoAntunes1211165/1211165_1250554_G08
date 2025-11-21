package pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational;

import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@EqualsAndHashCode
public class IsbnDocument {

    @Size(min = 10, max = 13)
    @Getter
    @Setter
    @Field("isbn")
    private String isbn;

    public IsbnDocument() {}

    public IsbnDocument(String isbn) {
        this.isbn = isbn;
    }

}
