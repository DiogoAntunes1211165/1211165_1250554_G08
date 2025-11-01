package pt.psoft.g1.psoftg1.unitTests.lendingmanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.lendingmanagement.api.*;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.*;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LendingControllerTest {

    @Mock private LendingService lendingService;
    @Mock private ReaderService readerService;
    @Mock private UserService userService;
    @Mock private ConcurrencyService concurrencyService;
    @Mock private LendingViewMapper lendingViewMapper;

    @InjectMocks
    private LendingController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldReturnCreatedResponse() {
        // Arrange
        CreateLendingRequest request = new CreateLendingRequest();
        Lending lending = mock(Lending.class);
        when(lending.getLendingNumber()).thenReturn("2025/001");
        when(lending.getVersion()).thenReturn(1L);

        when(lendingService.create(request)).thenReturn(lending);
        when(lendingViewMapper.toLendingView(lending)).thenReturn(new LendingView());

        // Mock ServletUriComponentsBuilder
        ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
        var uriComponents = mock(org.springframework.web.util.UriComponents.class);
        when(builder.pathSegment(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/lendings/2025/001"));

        try (MockedStatic<ServletUriComponentsBuilder> mocked = mockStatic(ServletUriComponentsBuilder.class)) {
            mocked.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(builder);

            // Act
            ResponseEntity<LendingView> response = controller.create(request);

            // Assert
            assertEquals(201, response.getStatusCodeValue());
            assertEquals(MediaType.parseMediaType("application/hal+json"), response.getHeaders().getContentType());
            assertNotNull(response.getBody());
            verify(lendingService).create(request);
        }
    }

    @Test
    void findByLendingNumber_shouldReturnOkForLibrarian() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        Lending lending = mock(Lending.class);
        when(lending.getVersion()).thenReturn(1L);
        when(lendingService.findByLendingNumber("2025/1")).thenReturn(Optional.of(lending));

        User librarian = mock(Librarian.class);
        when(userService.getAuthenticatedUser(auth)).thenReturn(librarian);
        when(lendingViewMapper.toLendingView(lending)).thenReturn(new LendingView());

        // Mock static builder
        ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
        var uriComponents = mock(org.springframework.web.util.UriComponents.class);
        when(builder.build()).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/lendings/2025/1"));
        try (MockedStatic<ServletUriComponentsBuilder> mocked = mockStatic(ServletUriComponentsBuilder.class)) {
            mocked.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(builder);

            // Act
            ResponseEntity<LendingView> response = controller.findByLendingNumber(auth, 2025, 1);

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            verify(lendingService).findByLendingNumber("2025/1");
        }
    }

    @Test
    void setLendingReturned_shouldReturnOk() {
        // Arrange
        WebRequest webRequest = mock(WebRequest.class);
        Authentication auth = mock(Authentication.class);
        when(webRequest.getHeader(ConcurrencyService.IF_MATCH)).thenReturn("1");
        when(concurrencyService.getVersionFromIfMatchHeader("1")).thenReturn(1L);

        Lending lending = mock(Lending.class);
        ReaderDetails reader = mock(ReaderDetails.class);
        when(reader.getReaderNumber()).thenReturn("R1");
        when(lending.getReaderDetails()).thenReturn(reader);
        when(lending.getVersion()).thenReturn(2L);
        when(lendingService.findByLendingNumber("2025/1")).thenReturn(Optional.of(lending));
        when(lendingService.setReturned(eq("2025/1"), any(), anyLong())).thenReturn(lending);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("u1");
        when(userService.getAuthenticatedUser(auth)).thenReturn(user);
        when(readerService.findByUsername("u1")).thenReturn(Optional.of(reader));

        when(lendingViewMapper.toLendingView(lending)).thenReturn(new LendingView());

        // Act
        ResponseEntity<LendingView> response = controller.setLendingReturned(webRequest, auth, 2025, 1, new SetLendingReturnedRequest());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.parseMediaType("application/hal+json"), response.getHeaders().getContentType());
        verify(lendingService).setReturned(eq("2025/1"), any(), eq(1L));
    }

    @Test
    void getAvgDuration_shouldReturnOk() {
        // Arrange
        when(lendingService.getAverageDuration()).thenReturn(3.5);
        when(lendingViewMapper.toLendingsAverageDurationView(3.5)).thenReturn(new LendingsAverageDurationView());

        // Act
        ResponseEntity<LendingsAverageDurationView> response = controller.getAvgDuration();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getOverdueLendings_shouldReturnList() {
        // Arrange
        Page page = mock(Page.class);
        Lending l1 = mock(Lending.class);
        Lending l2 = mock(Lending.class);
        when(lendingService.getOverdue(page)).thenReturn(List.of(l1, l2));
        when(lendingViewMapper.toLendingView(anyList())).thenReturn(List.of(new LendingView(), new LendingView()));

        // Act
        ListResponse<LendingView> result = controller.getOverdueLendings(page);

        // Assert
        assertEquals(2, result.getItems().size());
        verify(lendingService).getOverdue(page);
    }

    @Test
    void searchReaders_shouldReturnList() {
        // Arrange
        SearchRequest<SearchLendingQuery> req = mock(SearchRequest.class);
        when(req.getPage()).thenReturn(mock(Page.class));
        when(req.getQuery()).thenReturn(mock(SearchLendingQuery.class));
        when(lendingService.searchLendings(any(), any())).thenReturn(List.of(mock(Lending.class)));
        when(lendingViewMapper.toLendingView(anyList())).thenReturn(List.of(new LendingView()));

        // Act
        ListResponse<LendingView> result = controller.searchReaders(req);

        // Assert
        assertEquals(1, result.getItems().size());
        verify(lendingService).searchLendings(any(), any());
    }
}

