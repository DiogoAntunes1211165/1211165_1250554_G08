package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

public class ReaderNumberDocument implements Serializable {

    @Field("reader_number")
    private String readerNumber;

    public ReaderNumberDocument(int year, int number) {
        this.readerNumber = year + "/" + number;
    }

    public ReaderNumberDocument(int number) {
        this.readerNumber = java.time.LocalDate.now().getYear() + "/" + number;
    }

    public int getReaderNumber() {
        return Integer.parseInt(this.readerNumber.split("/")[1]);
    }

    protected ReaderNumberDocument() {}

    public String toString() {
        return this.readerNumber;
    }
}
