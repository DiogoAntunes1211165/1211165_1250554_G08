package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class EmailAddress {

    String address;

    protected EmailAddress() {}
}
