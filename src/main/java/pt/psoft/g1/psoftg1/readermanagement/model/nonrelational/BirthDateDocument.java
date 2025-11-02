package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

@NoArgsConstructor
public class BirthDateDocument {

    @Getter
    @Field("birth_date")
    private LocalDate birthDate;

    @Transient
    private final String dateFormatRegexPattern = "\\d{4}-\\d{2}-\\d{2}";

    @Transient
    @Value("${minimumReaderAge}")
    private int minimumAge;

    public BirthDateDocument(int year, int month, int day) {
        setBirthDate(year, month, day);
    }

    public BirthDateDocument(String birthDate) {
        if (!birthDate.matches(dateFormatRegexPattern)) {
            throw new IllegalArgumentException("Provided birth date is not in a valid format. Use yyyy-MM-dd");
        }

        String[] dateParts = birthDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        setBirthDate(year, month, day);
    }

    private void setBirthDate(int year, int month, int day) {
        LocalDate minimumAgeDate = LocalDate.now().minusYears(minimumAge);
        LocalDate userDate = LocalDate.of(year, month, day);

        if (userDate.isAfter(minimumAgeDate)) {
            throw new AccessDeniedException("User must be at least " + minimumAge + " years old");
        }

        this.birthDate = userDate;
    }

    @Override
    public String toString() {
        return String.format("%d-%02d-%02d",
                this.birthDate.getYear(),
                this.birthDate.getMonthValue(),
                this.birthDate.getDayOfMonth());
    }
}
