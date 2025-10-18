package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * The {@code Fine} class models a fine applied when a lending is past its due date.
 * <p>It stores its current value, and the associated {@code Lending}.
 * @author  rmfranca*/
@Getter

public class Fine {

    private Long pk;


    private int fineValuePerDayInCents;

    /**Fine value in Euro cents*/

    int centsValue;

    @Setter

    private Lending lending;

    /**
     * Constructs a new {@code Fine} object. Sets the current value of the fine,
     * as well as the fine value per day at the time of creation.
     * @param   lending transaction which generates this fine.
     * */
    public Fine(Lending lending) {
        if(lending.getDaysDelayed() <= 0)
            throw new IllegalArgumentException("Lending is not overdue");
        fineValuePerDayInCents = lending.getFineValuePerDayInCents();
        centsValue = fineValuePerDayInCents * lending.getDaysDelayed();
        this.lending = Objects.requireNonNull(lending);
    }

    /**Protected empty constructor for ORM only.*/
    protected Fine() {}
}
