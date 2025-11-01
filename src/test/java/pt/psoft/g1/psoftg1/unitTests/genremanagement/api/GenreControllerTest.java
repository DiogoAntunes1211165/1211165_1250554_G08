package pt.psoft.g1.psoftg1.unitTests.genremanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.api.*;
import pt.psoft.g1.psoftg1.genremanagement.services.*;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreControllerTest {

    @Mock private GenreService genreService;
    @Mock private GenreViewMapper genreViewMapper;

    @InjectMocks
    private GenreController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAverageLendings_shouldReturnListResponse() {
        // Arrange
        SearchRequest<GetAverageLendingsQuery> request = mock(SearchRequest.class);
        GetAverageLendingsQuery query = mock(GetAverageLendingsQuery.class);
        Page page = mock(Page.class);
        when(request.getQuery()).thenReturn(query);
        when(request.getPage()).thenReturn(page);

        List<GenreLendingsDTO> domainList = List.of(mock(GenreLendingsDTO.class));
        List<GenreLendingsView> viewList = List.of(new GenreLendingsView());
        when(genreService.getAverageLendings(query, page)).thenReturn(domainList);
        when(genreViewMapper.toGenreAvgLendingsView(domainList)).thenReturn(viewList);

        // Act
        ListResponse<GenreLendingsView> response = controller.getAverageLendings(request);

        // Assert
        assertEquals(1, response.getItems().size());
        verify(genreService).getAverageLendings(query, page);
    }

    @Test
    void getTop_shouldReturnListResponse() {
        // Arrange
        List<GenreBookCountDTO> domainList = List.of(mock(GenreBookCountDTO.class));
        List<GenreBookCountView> viewList = List.of(new GenreBookCountView());
        when(genreService.findTopGenreByBooks()).thenReturn(domainList);
        when(genreViewMapper.toGenreBookCountView(domainList)).thenReturn(viewList);

        // Act
        ListResponse<GenreBookCountView> response = controller.getTop();

        // Assert
        assertEquals(1, response.getItems().size());
        verify(genreService).findTopGenreByBooks();
    }

    @Test
    void getTop_shouldThrowNotFoundException_whenEmpty() {
        // Arrange
        when(genreService.findTopGenreByBooks()).thenReturn(List.<GenreBookCountDTO>of());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> controller.getTop());
    }

    @Test
    void getLendingsPerMonthLastYearByGenre_shouldReturnListResponse() {
        // Arrange
        List<GenreLendingsPerMonthDTO> domainList = List.of(mock(GenreLendingsPerMonthDTO.class));
        List<GenreLendingsCountPerMonthView> viewList = List.of(new GenreLendingsCountPerMonthView(2024, 1, List.of(new GenreLendingsView())));
        when(genreService.getLendingsPerMonthLastYearByGenre()).thenReturn(domainList);
        when(genreViewMapper.toGenreLendingsCountPerMonthView(domainList)).thenReturn(viewList);

        // Act
        ListResponse<GenreLendingsCountPerMonthView> response = controller.getLendingsPerMonthLastYearByGenre();

        // Assert
        assertEquals(1, response.getItems().size());
        verify(genreService).getLendingsPerMonthLastYearByGenre();
    }

    @Test
    void getLendingsPerMonthLastYearByGenre_shouldThrowNotFound_whenEmpty() {
        // Arrange
        when(genreService.getLendingsPerMonthLastYearByGenre()).thenReturn(List.<GenreLendingsPerMonthDTO>of());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> controller.getLendingsPerMonthLastYearByGenre());
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldReturnListResponse() {
        // Arrange
        List<GenreLendingsPerMonthDTO> domainList = List.of(mock(GenreLendingsPerMonthDTO.class));
        List<GenreLendingsAvgPerMonthView> viewList = List.of(new GenreLendingsAvgPerMonthView(2024, 1, List.of(new GenreLendingsView())));
        when(genreService.getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31")).thenReturn(domainList);
        when(genreViewMapper.toGenreLendingsAveragePerMonthView(domainList)).thenReturn(viewList);

        // Act
        ListResponse<GenreLendingsAvgPerMonthView> response =
                controller.getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31");

        // Assert
        assertEquals(1, response.getItems().size());
        verify(genreService).getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31");
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldThrowNotFound_whenEmpty() {
        // Arrange
        when(genreService.getLendingsAverageDurationPerMonth(anyString(), anyString())).thenReturn(List.<GenreLendingsPerMonthDTO>of());

        // Act + Assert
        assertThrows(NotFoundException.class,
                () -> controller.getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31"));
    }
}
