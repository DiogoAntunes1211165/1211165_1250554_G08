package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Data
@Schema(description = "A lending saga creation response")
public class SagaCreationResponse {

    private String lendingNumber;

    private String status;

    private String error;

    @Setter
    @Getter
    private Map<String, Object> _links = new HashMap<>();

}
