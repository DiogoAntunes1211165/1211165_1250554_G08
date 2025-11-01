package pt.psoft.g1.psoftg1.unitTests.lendingmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingServiceImpl;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SearchLendingQuery;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedRequest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LendingServiceImplTest {

    @Mock private LendingRepository lendingRepository;
    @Mock private FineRepository fineRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ReaderRepository readerRepository;

    @InjectMocks
    private LendingServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        setPrivateField("lendingDurationInDays", 14);
        setPrivateField("fineValuePerDayInCents", 50);
    }

    private void setPrivateField(String fieldName, Object value) {
        try {
            Field field = LendingServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create_shouldThrow_whenReaderHasLateBooks() {
        Lending lateLending = mock(Lending.class);
        when(lateLending.getDaysDelayed()).thenReturn(2);
        when(lendingRepository.listOutstandingByReaderNumber("123"))
                .thenReturn(List.of(lateLending));

        CreateLendingRequest req = new CreateLendingRequest("123", "978-0000");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(LendingForbiddenException.class)
                .hasMessageContaining("past their due date");
    }

    @Test
    void create_shouldThrow_whenReaderHasThreeOutstandingBooks() {
        Lending lending = mock(Lending.class);
        when(lending.getDaysDelayed()).thenReturn(0);
        when(lendingRepository.listOutstandingByReaderNumber("123"))
                .thenReturn(List.of(lending, lending, lending));

        CreateLendingRequest req = new CreateLendingRequest("123", "978-0000");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(LendingForbiddenException.class)
                .hasMessageContaining("three books outstanding");
    }

    @Test
    void create_shouldReturnSavedLending_whenValid() {
        when(lendingRepository.listOutstandingByReaderNumber("123")).thenReturn(List.of());
        // Book and ReaderDetails have protected/default constructors; use mocks instead
        Book book = mock(Book.class);
        ReaderDetails reader = mock(ReaderDetails.class);
        when(bookRepository.findByIsbn("978-0000")).thenReturn(Optional.of(book));
        when(readerRepository.findByReaderNumber("123")).thenReturn(Optional.of(reader));
        when(lendingRepository.getCountFromCurrentYear()).thenReturn(4);
        Lending saved = mock(Lending.class);
        when(lendingRepository.save(any(Lending.class))).thenReturn(saved);

        CreateLendingRequest req = new CreateLendingRequest("123", "978-0000");
        Lending result = service.create(req);

        assertThat(result).isEqualTo(saved);
        verify(lendingRepository).save(any(Lending.class));
    }

    @Test
    void setReturned_shouldCreateFine_whenDelayed() {
        Lending lending = mock(Lending.class);
        when(lending.getDaysDelayed()).thenReturn(5);
        when(lendingRepository.findByLendingNumber("2024/1")).thenReturn(Optional.of(lending));
        when(lendingRepository.save(lending)).thenReturn(lending);

        SetLendingReturnedRequest req = new SetLendingReturnedRequest("ok");
        service.setReturned("2024/1", req, 1L);

        verify(fineRepository).save(any(Fine.class));
        verify(lendingRepository).save(lending);
    }

    @Test
    void getAverageDuration_shouldFormatCorrectly() {
        when(lendingRepository.getAverageDuration()).thenReturn(3.14159);
        Double result = service.getAverageDuration();
        assertThat(result).isEqualTo(3.1);
    }

    @Test
    void getOverdue_shouldReturnList() {
        when(lendingRepository.getOverdue(any(Page.class)))
                .thenReturn(List.of(mock(Lending.class)));
        List<Lending> result = service.getOverdue(new Page(1, 10));
        assertThat(result).hasSize(1);
    }

    @Test
    void searchLendings_shouldThrowOnInvalidDateFormat() {
        SearchLendingQuery q = new SearchLendingQuery("", "", null, "2024/01/01", null);
        assertThatThrownBy(() -> service.searchLendings(new Page(1, 10), q))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected format is YYYY-MM-DD");
    }
}
