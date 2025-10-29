package pt.psoft.g1.psoftg1.unitTests.genremanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreController;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreBookCountView;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreLendingsAvgPerMonthView;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreLendingsCountPerMonthView;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreLendingsView;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewMapper;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GetAverageLendingsQuery;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@WebMvcTest(GenreController.class)
public class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @MockBean
    private GenreViewMapper genreViewMapper;

    @Test
    @DisplayName("GET /api/genres/top5 - success returns list")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetTop_Success() throws Exception {
        // Arrange
        var dto = new GenreBookCountDTO("Sci-Fi", 5);
        var view = new GenreBookCountView();

        // minimal GenreView inside view (mapper will set the object, but we only check outer fields)
        when(genreService.findTopGenreByBooks()).thenReturn(List.of(dto));
        when(genreViewMapper.toGenreBookCountView(List.of(dto))).thenReturn(List.of(view));

        // Act & Assert
        mockMvc.perform(get("/api/genres/top5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/genres/top5 - NotFound when empty")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetTop_NotFound() throws Exception {
        when(genreService.findTopGenreByBooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/genres/top5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/genres/lendingsPerMonthLastTwelveMonths - success")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetLendingsPerMonthLastYear_Success() throws Exception {
        var values = List.of(new GenreLendingsDTO("Sci-Fi", 1L));
        var dto = new GenreLendingsPerMonthDTO(2025, 1, values);
        var view = new GenreLendingsCountPerMonthView(2025, 1, List.of(new GenreLendingsView()));

        when(genreService.getLendingsPerMonthLastYearByGenre()).thenReturn(List.of(dto));
        when(genreViewMapper.toGenreLendingsCountPerMonthView(List.of(dto))).thenReturn(List.of(view));

        mockMvc.perform(get("/api/genres/lendingsPerMonthLastTwelveMonths").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/genres/lendingsPerMonthLastTwelveMonths - NotFound when empty")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetLendingsPerMonthLastYear_NotFound() throws Exception {
        when(genreService.getLendingsPerMonthLastYearByGenre()).thenReturn(List.of());

        mockMvc.perform(get("/api/genres/lendingsPerMonthLastTwelveMonths").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/genres/lendingsAverageDurationPerMonth - success")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetLendingsAverageDurationPerMonth_Success() throws Exception {
        var values = List.of(new GenreLendingsDTO("Sci-Fi", 1.2));
        var dto = new GenreLendingsPerMonthDTO(2025, 2, values);
        var view = new GenreLendingsAvgPerMonthView(2025, 2, List.of(new GenreLendingsView()));

        when(genreService.getLendingsAverageDurationPerMonth("2025-01-01", "2025-12-31")).thenReturn(List.of(dto));
        when(genreViewMapper.toGenreLendingsAveragePerMonthView(List.of(dto))).thenReturn(List.of(view));

        mockMvc.perform(get("/api/genres/lendingsAverageDurationPerMonth")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/genres/lendingsAverageDurationPerMonth - NotFound when empty")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetLendingsAverageDurationPerMonth_NotFound() throws Exception {
        when(genreService.getLendingsAverageDurationPerMonth("2025-01-01", "2025-12-31")).thenReturn(List.of());

        mockMvc.perform(get("/api/genres/lendingsAverageDurationPerMonth")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/genres/avgLendingsPerGenre - success")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAverageLendings_Success() throws Exception {
        var dto = new GenreLendingsDTO("Sci-Fi", 2.0);
        var view = new GenreLendingsView();

        // build SearchRequest JSON
        // GetAverageLendingsQuery requires year and month (month between 1 and 12)
        String body = "{\"query\": {\"year\": 2025, \"month\": 2}, \"page\": {\"number\": 1, \"limit\": 10}}";

        when(genreService.getAverageLendings(any(GetAverageLendingsQuery.class), any())).thenReturn(List.of(dto));
        when(genreViewMapper.toGenreAvgLendingsView(List.of(dto))).thenReturn(List.of(view));

        mockMvc.perform(post("/api/genres/avgLendingsPerGenre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("POST /api/genres/avgLendingsPerGenre - invalid payload returns 400")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAverageLendings_InvalidPayload() throws Exception {
        // Missing 'query' field -> SearchRequest requires not null
        String body = "{\"page\": {\"number\": 1, \"limit\": 10}}";

        mockMvc.perform(post("/api/genres/avgLendingsPerGenre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/genres/avgLendingsPerGenre - controller passes parsed query to service")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAverageLendings_VerifyServiceCall() throws Exception {
        var dto = new GenreLendingsDTO("Sci-Fi", 2.0);
        var view = new GenreLendingsView();

        String body = "{\"query\": {\"year\": 2025, \"month\": 2}, \"page\": {\"number\": 1, \"limit\": 10}}";

        when(genreService.getAverageLendings(any(GetAverageLendingsQuery.class), any())).thenReturn(List.of(dto));
        when(genreViewMapper.toGenreAvgLendingsView(List.of(dto))).thenReturn(List.of(view));

        mockMvc.perform(post("/api/genres/avgLendingsPerGenre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        ArgumentCaptor<GetAverageLendingsQuery> captor = ArgumentCaptor.forClass(GetAverageLendingsQuery.class);
        verify(genreService).getAverageLendings(captor.capture(), any());
        GetAverageLendingsQuery captured = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(captured.getYear()).isEqualTo(2025);
        org.assertj.core.api.Assertions.assertThat(captured.getMonth()).isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/genres/top5 - unauthorized returns 401/403")
    void testUnauthorizedAccess() throws Exception {
        // No @WithMockUser -> should be unauthorized
        mockMvc.perform(get("/api/genres/top5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/genres/top5 - service throws runtime -> 500")
    @WithMockUser(username = "user", roles = {"USER"})
    void testServiceThrowsRuntimeException() throws Exception {
        when(genreService.findTopGenreByBooks()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/genres/top5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is5xxServerError());
    }

}
