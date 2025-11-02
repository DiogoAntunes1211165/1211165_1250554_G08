package pt.psoft.g1.psoftg1.readermanagement.model.relational;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
public class EmailAddressEntity implements Serializable {

    String address;

    protected EmailAddressEntity() {}
}
