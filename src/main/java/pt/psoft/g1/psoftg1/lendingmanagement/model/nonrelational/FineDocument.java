package pt.psoft.g1.psoftg1.lendingmanagement.model.nonrelational;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@Getter
@Document(collection = "fines")
public class FineDocument {

    @Id
    private Long pk;

    /** Fine value per day in cents is persisted but not updatable */
    @PositiveOrZero
    @Field("fine_value_per_day_in_cents")
    private int fineValuePerDayInCents;

    /** Fine value in Euro cents */
    @PositiveOrZero
    @Field("cents_value")
    private int centsValue;

    @Setter
    @Field("lending")
    @DBRef
    private LendingDocument lendingDocument;

    /**
     * Constructs a new {@code Fine} object. Sets the current value of the fine,
     * as well as the fine value per day at the time of creation.
     *
     * @param lending transaction which generates this fine.
     */
    public FineDocument(LendingDocument lending) {
        if (lending.getDaysDelayed() <= 0) {
            throw new IllegalArgumentException("Lending is not overdue");
        }
        this.fineValuePerDayInCents = lending.getFineValuePerDayInCents();
        this.centsValue = fineValuePerDayInCents * lending.getDaysDelayed();
        this.lendingDocument = Objects.requireNonNull(lending);
    }

    /** Protected empty constructor for ORM only. */
    protected FineDocument() {
        this.fineValuePerDayInCents = 0;
    }

}
