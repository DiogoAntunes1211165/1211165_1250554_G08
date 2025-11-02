package pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational;

import lombok.Getter;

import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
public class DescriptionDocument {

    @Getter
    @Setter
    @Field("description")
    private String description;

    public DescriptionDocument(String description) {
        this.description = description;
    }

    protected DescriptionDocument() {}

}
