package pt.psoft.g1.psoftg1.unitTests.lendingmanagement.model;

import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FineTest {

    @Test
    void shouldCreateFineSuccessfully_whenLendingIsOverdue() {
        // Arrange
        Lending lending = mock(Lending.class);
        when(lending.getDaysDelayed()).thenReturn(3);
        when(lending.getFineValuePerDayInCents()).thenReturn(50);

        // Act
        Fine fine = new Fine(lending);

        // Assert
        assertThat(fine.getLending()).isEqualTo(lending);
        assertThat(fine.getFineValuePerDayInCents()).isEqualTo(50);
        assertThat(fine.getCentsValue()).isEqualTo(150); // 3 * 50
    }

    @Test
    void shouldThrowException_whenLendingIsNotOverdue() {
        // Arrange
        Lending lending = mock(Lending.class);
        when(lending.getDaysDelayed()).thenReturn(0);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Fine(lending));
    }

    @Test
    void shouldThrowException_whenLendingIsNull() {
        // Act + Assert
        assertThrows(NullPointerException.class, () -> new Fine(null));
    }

    @Test
    void shouldAllowChangingLendingWithSetter() {
        // Arrange
        Lending lending1 = mock(Lending.class);
        when(lending1.getDaysDelayed()).thenReturn(2);
        when(lending1.getFineValuePerDayInCents()).thenReturn(50);

        Lending lending2 = mock(Lending.class);
        when(lending2.getDaysDelayed()).thenReturn(5);
        when(lending2.getFineValuePerDayInCents()).thenReturn(100);

        Fine fine = new Fine(lending1);

        // Act
        fine.setLending(lending2);

        // Assert
        assertThat(fine.getLending()).isEqualTo(lending2);
    }

    @Test
    void protectedConstructor_shouldNotThrow() throws Exception {
        // Use reflection to access the protected no-arg constructor
        Constructor<Fine> ctor = Fine.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Fine fine = ctor.newInstance();
        assertThat(fine).isNotNull();
    }
}
