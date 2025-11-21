package pt.psoft.g1.psoftg1.unitTests.readermanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.external.service.ApiNinjasService;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderController;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderQuoteView;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ReaderControllerTest {

    @Mock
    private ReaderService readerService;

    @Mock
    private UserService userService;

    @Mock
    private ReaderViewMapper readerViewMapper;

    @Mock
    private ApiNinjasService apiNinjasService;

    @InjectMocks
    private ReaderController readerController;

    private ReaderDetails readerDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        readerDetails = mock(ReaderDetails.class);
        when(readerDetails.getReaderNumber()).thenReturn("2024/1");
        when(readerDetails.getVersion()).thenReturn(1L);
        when(readerDetails.getBirthDate()).thenReturn(new BirthDate(1999, 5, 12));
    }

    @Test
    void findByReaderNumber_shouldReturnReaderQuoteView_whenReaderExists() {
        // Arrange
        when(readerService.findByReaderNumber("2024/1")).thenReturn(Optional.of(readerDetails));

        ReaderQuoteView mockQuoteView = new ReaderQuoteView();
        when(readerViewMapper.toReaderQuoteView(readerDetails)).thenReturn(mockQuoteView);
        when(apiNinjasService.getRandomEventFromYearMonth(1999, 5)).thenReturn("Some random quote");

        // Act
        ResponseEntity<ReaderQuoteView> response = readerController.findByReaderNumber(2024, 1);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getETag()).isEqualTo("\"1\"");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuote()).isEqualTo("Some random quote");

    }

    @Test
    void findByReaderNumber_shouldThrowNotFound_whenReaderDoesNotExist() {
        // Arrange
        when(readerService.findByReaderNumber(anyString())).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> readerController.findByReaderNumber(2024, 99));
    }
}
