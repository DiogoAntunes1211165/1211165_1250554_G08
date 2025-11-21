package pt.psoft.g1.psoftg1.unitTests.genremanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.*;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceImplTest {

    @Mock private GenreRepository genreRepository;
    @InjectMocks private GenreServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByString_shouldReturnOptionalGenre() {
        Genre genre = mock(Genre.class);
        when(genreRepository.findByString("Fantasy")).thenReturn(Optional.of(genre));

        Optional<Genre> result = service.findByString("Fantasy");

        assertTrue(result.isPresent());
        assertEquals(genre, result.get());
        verify(genreRepository).findByString("Fantasy");
    }

    @Test
    void findAll_shouldReturnAllGenres() {
        Iterable<Genre> genres = List.of(mock(Genre.class));
        when(genreRepository.findAll()).thenReturn(genres);

        Iterable<Genre> result = service.findAll();

        assertEquals(genres, result);
        verify(genreRepository).findAll();
    }

    @Test
    void findTopGenreByBooks_shouldReturnList() {
        List<GenreBookCountDTO> expected = List.of(mock(GenreBookCountDTO.class));
        when(genreRepository.findTop5GenreByBookCount(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(expected));

        List<GenreBookCountDTO> result = service.findTopGenreByBooks();

        assertEquals(1, result.size());
        verify(genreRepository).findTop5GenreByBookCount(any(PageRequest.class));
    }

    @Test
    void save_shouldCallRepositorySave() {
        Genre genre = mock(Genre.class);
        when(genreRepository.save(genre)).thenReturn(genre);

        Genre result = service.save(genre);

        assertEquals(genre, result);
        verify(genreRepository).save(genre);
    }

    @Test
    void getLendingsPerMonthLastYearByGenre_shouldReturnList() {
        List<GenreLendingsPerMonthDTO> expected = List.of(mock(GenreLendingsPerMonthDTO.class));
        when(genreRepository.getLendingsPerMonthLastYearByGenre()).thenReturn(expected);

        List<GenreLendingsPerMonthDTO> result = service.getLendingsPerMonthLastYearByGenre();

        assertEquals(expected, result);
        verify(genreRepository).getLendingsPerMonthLastYearByGenre();
    }

    @Test
    void getAverageLendings_shouldUseDefaultPage_whenNull() {
        GetAverageLendingsQuery query = mock(GetAverageLendingsQuery.class);
        when(query.getYear()).thenReturn(2024);
        when(query.getMonth()).thenReturn(5);

        List<GenreLendingsDTO> expected = List.of(mock(GenreLendingsDTO.class));
        when(genreRepository.getAverageLendingsInMonth(any(LocalDate.class), any(Page.class)))
                .thenReturn(expected);

        List<GenreLendingsDTO> result = service.getAverageLendings(query, null);

        assertEquals(expected, result);
        verify(genreRepository).getAverageLendingsInMonth(any(LocalDate.class), any(Page.class));
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldReturnList() {
        List<GenreLendingsPerMonthDTO> expected = List.of(mock(GenreLendingsPerMonthDTO.class));
        when(genreRepository.getLendingsAverageDurationPerMonth(any(), any()))
                .thenReturn(expected);

        List<GenreLendingsPerMonthDTO> result =
                service.getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31");

        assertEquals(expected, result);
        verify(genreRepository).getLendingsAverageDurationPerMonth(any(), any());
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldThrow_whenEmpty() {
        when(genreRepository.getLendingsAverageDurationPerMonth(any(), any()))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> service.getLendingsAverageDurationPerMonth("2024-01-01", "2024-12-31"));
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldThrow_whenInvalidDateFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getLendingsAverageDurationPerMonth("2024/01/01", "2024-12-31"));
    }

    @Test
    void getLendingsAverageDurationPerMonth_shouldThrow_whenStartAfterEnd() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getLendingsAverageDurationPerMonth("2025-12-31", "2025-01-01"));
    }
}
