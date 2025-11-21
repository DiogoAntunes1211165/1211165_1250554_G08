package pt.psoft.g1.psoftg1.integrationTests.lendingmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingController;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingView;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingsAverageDurationView;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.*;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LendingController.class)
class LendingControllerOpaqueTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LendingService lendingService;

    @MockBean
    private ReaderService readerService;

    @MockBean
    private UserService userService;

    @MockBean
    private ConcurrencyService concurrencyService;

    @MockBean
    private LendingViewMapper lendingViewMapper;

    @Test
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN"})
    void testCreateLending() throws Exception {
        Lending mockedLending = Mockito.mock(Lending.class);
        when(mockedLending.getLendingNumber()).thenReturn("2025/1");
        when(mockedLending.getVersion()).thenReturn(1L);
        when(lendingService.create(any(CreateLendingRequest.class))).thenReturn(mockedLending);

        LendingView view = new LendingView();
        view.setLendingNumber("2025/1");
        when(lendingViewMapper.toLendingView(mockedLending)).thenReturn(view);

        // Create a valid CreateLendingRequest using actual fields: isbn (10-13 chars) and readerNumber (6-16 chars)
        CreateLendingRequest request = new CreateLendingRequest();
        request.setIsbn("9781234567890"); // 13 chars
        request.setReaderNumber("123456"); // 6 chars, satisfies validation

        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.lendingNumber").value("2025/1"));
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetLendingNotFound() throws Exception {
        when(lendingService.findByLendingNumber(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lendings/2025/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetLendingAccessDenied() throws Exception {
        Lending mockedLending = Mockito.mock(Lending.class);
        // ReaderDetails has protected constructor and private setters; use mocks instead
        ReaderDetails lendingReader = Mockito.mock(ReaderDetails.class);
        when(lendingReader.getReaderNumber()).thenReturn("2"); // different from logged reader
        when(mockedLending.getReaderDetails()).thenReturn(lendingReader);
        when(mockedLending.getVersion()).thenReturn(1L);
        when(lendingService.findByLendingNumber(anyString())).thenReturn(Optional.of(mockedLending));

        User loggedUser = Mockito.mock(User.class);
        when(userService.getAuthenticatedUser(any())).thenReturn(loggedUser);
        // ensure getUsername() returns a value the controller can use
        when(loggedUser.getUsername()).thenReturn("readerUser");
        ReaderDetails loggedReader = Mockito.mock(ReaderDetails.class);
        when(loggedReader.getReaderNumber()).thenReturn("1"); // different from lending
        when(readerService.findByUsername("readerUser")).thenReturn(Optional.of(loggedReader));

        mockMvc.perform(get("/api/lendings/2025/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testSetLendingReturnedBadIfMatch() throws Exception {
        SetLendingReturnedRequest request = new SetLendingReturnedRequest();

        mockMvc.perform(patch("/api/lendings/2025/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetAvgDuration() throws Exception {
        LendingsAverageDurationView avg = new LendingsAverageDurationView();
        avg.setLendingsAverageDuration(1.0);
        when(lendingViewMapper.toLendingsAverageDurationView(any(Double.class))).thenReturn(avg);

        mockMvc.perform(get("/api/lendings/avgDuration"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testGetOverdueNotFound() throws Exception {
        when(lendingService.getOverdue(any(Page.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/lendings/overdue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Page())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "reader", roles = {"READER"})
    void testSearchLendings() throws Exception {
        when(lendingService.searchLendings(any(), any())).thenReturn(Collections.emptyList());
        when(lendingViewMapper.toLendingView(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/lendings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":{},\"query\":{}}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
}
