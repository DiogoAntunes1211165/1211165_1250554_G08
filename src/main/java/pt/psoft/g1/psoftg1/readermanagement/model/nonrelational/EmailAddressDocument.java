package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
public class EmailAddressDocument {

    @Getter
    @Setter
    @Field("email_address")
    String address;

    protected EmailAddressDocument() {}
}
