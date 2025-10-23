package pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
public class TitleDocument {

    @Transient
    private final int TITLE_MAX_LENGTH = 128;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = TITLE_MAX_LENGTH)
    @Getter
    @Setter
    @Field("title")
    private String title;

    public TitleDocument() {}

    public TitleDocument(String title) {
        setTitle(title);
    }

}
