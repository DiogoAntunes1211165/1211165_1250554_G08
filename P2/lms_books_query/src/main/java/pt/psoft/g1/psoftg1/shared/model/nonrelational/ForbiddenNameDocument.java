package pt.psoft.g1.psoftg1.shared.model.nonrelational;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@Document(collection = "forbidden_names")
public class ForbiddenNameDocument {

    @Id
    private String id;

    @Getter
    @Setter
    @Field("forbidden_name")
    private String forbiddenName;

    public ForbiddenNameDocument(String name) {
        this.forbiddenName = name;
    }
}
