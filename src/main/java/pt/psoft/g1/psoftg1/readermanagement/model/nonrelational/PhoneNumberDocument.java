package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
public class PhoneNumberDocument {

    @Getter
    @Setter
    @Field("phone_number")
    private String phoneNumber;

    public PhoneNumberDocument(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    protected PhoneNumberDocument() {}
}
