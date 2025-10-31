package pt.psoft.g1.psoftg1.unitTests.genremanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.*;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    @BeforeEach
    void setup() {
    }

    @Test
    void testFindByString_Found() {
        Genre g = new Genre("Sci-Fi");
        when(genreRepository.findByString("Sci-Fi")).thenReturn(Optional.of(g));

        Optional<Genre> result = genreService.findByString("Sci-Fi");

        assertTrue(result.isPresent());
        assertEquals(g, result.get());
        verify(genreRepository).findByString("Sci-Fi");
    }

    @Test
    void testFindByString_NotFound() {
        when(genreRepository.findByString("Unknown")).thenReturn(Optional.empty());

        Optional<Genre> result = genreService.findByString("Unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_ReturnsAllGenres() {
        Genre g = new Genre("Fantasy");
        List<Genre> expected = List.of(g);
        when(genreRepository.findAll()).thenReturn(expected);

        Iterable<Genre> result = genreService.findAll();

        assertEquals(expected, result);
        verify(genreRepository).findAll();
    }

    @Test
    void testFindTopGenreByBooks() {
        List<GenreBookCountDTO> dtoList = List.of(new GenreBookCountDTO("Fantasy", 10));
        when(genreRepository.findTop5GenreByBookCount(any(Pageable.class))).thenReturn(new PageImpl<>(dtoList));

        List<GenreBookCountDTO> result = genreService.findTopGenreByBooks();

        assertEquals(dtoList, result);
        verify(genreRepository).findTop5GenreByBookCount(any(Pageable.class));
    }

    @Test
    void testSave_DelegatesToRepository() {
        Genre g = new Genre("Horror");
        when(genreRepository.save(g)).thenReturn(g);

        Genre result = genreService.save(g);

        assertEquals(g, result);
        verify(genreRepository).save(g);
    }

    @Test
    void testGetLendingsPerMonthLastYearByGenre() {
        GenreLendingsDTO v = new GenreLendingsDTO("Fantasy", 5L);
        GenreLendingsPerMonthDTO dto = new GenreLendingsPerMonthDTO(2024, 1, List.of(v));
        List<GenreLendingsPerMonthDTO> expected = List.of(dto);
        when(genreRepository.getLendingsPerMonthLastYearByGenre()).thenReturn(expected);

        List<GenreLendingsPerMonthDTO> result = genreService.getLendingsPerMonthLastYearByGenre();

        assertEquals(expected, result);
        verify(genreRepository).getLendingsPerMonthLastYearByGenre();
    }

    @Test
    void testGetAverageLendings_UsesDefaultPageWhenNull() {
        GetAverageLendingsQuery query = new GetAverageLendingsQuery(2024, 3);
        List<GenreLendingsDTO> expected = List.of(new GenreLendingsDTO("Fantasy", 2.5));
        LocalDate month = LocalDate.of(2024, 3, 1);

        when(genreRepository.getAverageLendingsInMonth(eq(month), any(Page.class))).thenReturn(expected);

        List<GenreLendingsDTO> result = genreService.getAverageLendings(query, null);

        assertEquals(expected, result);
        verify(genreRepository).getAverageLendingsInMonth(eq(month), any(Page.class));
    }

    @Test
    void testGetLendingsAverageDurationPerMonth_Success() {
        String start = "2020-01-01";
        String end = "2020-02-01";
        GenreLendingsPerMonthDTO dto = new GenreLendingsPerMonthDTO(2020,1,List.of(new GenreLendingsDTO("G",1L)));
        List<GenreLendingsPerMonthDTO> expected = List.of(dto);
        when(genreRepository.getLendingsAverageDurationPerMonth(LocalDate.parse(start), LocalDate.parse(end)))
                .thenReturn(expected);

        List<GenreLendingsPerMonthDTO> result = genreService.getLendingsAverageDurationPerMonth(start, end);

        assertEquals(expected, result);
        verify(genreRepository).getLendingsAverageDurationPerMonth(LocalDate.parse(start), LocalDate.parse(end));
    }

    @Test
    void testGetLendingsAverageDurationPerMonth_InvalidDateFormat_Throws() {
        assertThrows(IllegalArgumentException.class, () -> genreService.getLendingsAverageDurationPerMonth("bad", "2020-01-01"));
    }

    @Test
    void testGetLendingsAverageDurationPerMonth_StartAfterEnd_Throws() {
        String start = "2020-02-01";
        String end = "2020-01-01";
        assertThrows(IllegalArgumentException.class, () -> genreService.getLendingsAverageDurationPerMonth(start, end));
    }

    @Test
    void testGetLendingsAverageDurationPerMonth_EmptyList_ThrowsNotFound() {
        String start = "2020-01-01";
        String end = "2020-02-01";
        when(genreRepository.getLendingsAverageDurationPerMonth(LocalDate.parse(start), LocalDate.parse(end)))
                .thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> genreService.getLendingsAverageDurationPerMonth(start, end));
    }
}
