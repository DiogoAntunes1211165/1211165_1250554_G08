package pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.BookDocument;
import pt.psoft.g1.psoftg1.readermanagement.model.nonrelational.ReaderDetailsDocument;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Document(collection = "lendings")
public class LendingDocument {

    @Id
    private String id;

    @Getter
    @Setter
    @Field("lending_number")
    private LendingNumberDocument lendingNumberEntity; // Reference to the embedded LendingNumberEntity

    @Getter
    @Setter
    @NotNull
    @Field("book")
    private BookDocument book;

    @Getter
    @Setter
    @NotNull
    @Field("reader_details")
    private ReaderDetailsDocument readerDetails;


    @NotNull
    @Getter
    @Setter
    @Field("start_date")
    private LocalDate startDate;

    @NotNull
    @Getter
    @Setter
    @Field("limit_date")
    private LocalDate limitDate;

    @Getter
    @Setter
    @Field("returned_date")
    private LocalDate returnedDate;

    @Version
    private Long version;

    @Size(min = 0, max = 1024)
    @Field("commentary")
    private String commentary = null;

    @Getter @Setter
    @Field("fine_value_per_day_in_cents")
    private int fineValuePerDayInCents;

    @Getter @Setter
    @Field("days_until_return")
    private Integer daysUntilReturn;

    @Getter @Setter
    @Field("days_overdue")
    private Integer daysOverdue;

    public LendingDocument() {
    }

    public LendingDocument(BookDocument book, ReaderDetailsDocument readerDetails, LendingNumberDocument lendingNumber, LocalDate startDate, LocalDate limitDate, LocalDate returnedDate, int fineValuePerDayInCents) {
        this.book = book;
        this.readerDetails = readerDetails;
        this.lendingNumberEntity = lendingNumber;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        setDaysUntilReturn();
        setDaysOverdue();
    }

    private void setDaysUntilReturn() {
        int daysUntilReturn = (int) ChronoUnit.DAYS.between(LocalDate.now(), this.limitDate);
        this.daysUntilReturn = (this.returnedDate != null || daysUntilReturn < 0) ? null : daysUntilReturn;
    }

    private void setDaysOverdue() {
        int days = getDaysDelayed();
        this.daysOverdue = (days > 0) ? days : null;
    }

    public int getDaysDelayed() {
        if (this.returnedDate != null) {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, this.returnedDate), 0);
        } else {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, LocalDate.now()), 0);
        }
    }
}
