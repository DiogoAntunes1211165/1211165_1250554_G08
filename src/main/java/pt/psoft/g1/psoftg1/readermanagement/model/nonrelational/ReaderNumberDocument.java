package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Getter
public class ReaderNumberDocument implements Serializable {

    @Getter
    @Field("reader_number")
    private String readerNumber;

    public ReaderNumberDocument(int year, int number) {
        this.readerNumber = year + "/" + number;
    }

    public ReaderNumberDocument(int number) {
        this.readerNumber = java.time.LocalDate.now().getYear() + "/" + number;
    }

    protected ReaderNumberDocument() {}
}
