package pt.psoft.g1.psoftg1.unitTests.readermanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.CreateReaderRequest;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderMapper;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderServiceImpl;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReaderServiceTest {

    @Mock private ReaderRepository readerRepo;
    @Mock private UserRepository userRepo;
    @Mock private ReaderMapper readerMapper;
    @Mock private GenreRepository genreRepo;
    @Mock private ForbiddenNameRepository forbiddenNameRepository;
    @Mock private PhotoRepository photoRepository;

    @InjectMocks
    private ReaderServiceImpl readerService;

    private CreateReaderRequest createRequest;
    private Reader reader;
    private ReaderDetails readerDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createRequest = new CreateReaderRequest();
        createRequest.setUsername("john_doe");
        createRequest.setFullName("John Doe");
        createRequest.setInterestList(List.of("Fiction"));
        createRequest.setPhoto(mock(MultipartFile.class));

        reader = mock(Reader.class);
        readerDetails = mock(ReaderDetails.class);
    }

    @Test
    void create_shouldSaveReaderSuccessfully_whenDataIsValid() {
        // Arrange
        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained(anyString())).thenReturn(Collections.<ForbiddenName>emptyList());
        when(genreRepo.findByString("Fiction")).thenReturn(Optional.of(new Genre("Fiction")));
        when(readerRepo.getCountFromCurrentYear()).thenReturn(5);
        when(readerMapper.createReader(createRequest)).thenReturn(reader);
        when(readerMapper.createReaderDetails(anyInt(), eq(reader), eq(createRequest), anyString(), anyList())).thenReturn(readerDetails);
        when(readerRepo.save(readerDetails)).thenReturn(readerDetails);

        // Act
        ReaderDetails result = readerService.create(createRequest, "photo123.jpg");

        // Assert
        assertThat(result).isEqualTo(readerDetails);


    }


    @Test
    void create_shouldThrowConflictException_whenUsernameAlreadyExists() {
        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.of(reader));

        assertThrows(ConflictException.class, () -> readerService.create(createRequest, "photoURI"));
    }


    @Test
    void create_shouldThrowIllegalArgumentException_whenNameContainsForbiddenWord() {
        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained("John"))
                .thenReturn(List.of(new ForbiddenName("badword")));

        assertThrows(IllegalArgumentException.class, () -> readerService.create(createRequest, "photoURI"));
    }


    @Test
    void create_shouldThrowNotFoundException_whenGenreDoesNotExist() {
        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained(anyString())).thenReturn(Collections.<ForbiddenName>emptyList());
        when(genreRepo.findByString("Fiction")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readerService.create(createRequest, "photoURI"));
    }


    @Test
    void findTopByGenre_shouldThrowIllegalArgumentException_whenStartDateAfterEndDate() {
        var start = java.time.LocalDate.of(2025, 10, 1);
        var end = java.time.LocalDate.of(2025, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> readerService.findTopByGenre("Fiction", start, end));
    }


    @Test
    void removeReaderPhoto_shouldThrowNotFoundException_whenReaderNotFound() {
        when(readerRepo.findByReaderNumber("2024/1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readerService.removeReaderPhoto("2024/1", 1L));
    }


    @Test
    void removeReaderPhoto_shouldDeletePhotoSuccessfully() {
        var details = mock(ReaderDetails.class);
        var photo = mock(Photo.class);
        when(photo.getPhotoFile()).thenReturn("photo.jpg");
        when(details.getPhoto()).thenReturn(photo);
        when(readerRepo.findByReaderNumber("2024/1")).thenReturn(Optional.of(details));
        when(readerRepo.save(details)).thenReturn(details);

        var result = readerService.removeReaderPhoto("2024/1", 2L);

        assertThat(result).isPresent();
    }

    @Test
    void applyPatch_shouldUpdateAllFieldsCorrectly() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails rd = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"oldPhoto.jpg", new ArrayList<>());

        UpdateReaderRequest patch = new UpdateReaderRequest();
        patch.setFullName("New Name");
        patch.setBirthDate("2012-03-04");
        patch.setPhoneNumber("987654321");
        patch.setUsername("newUsername");
        patch.setPassword("newPassword");
        patch.setMarketing(true);
        patch.setThirdParty(true);

        List<Genre> newGenres = new ArrayList<>();
        newGenres.add(new Genre("Sci-Fi"));
        newGenres.add(new Genre("Romance"));

        assertDoesNotThrow(() -> rd.applyPatch(rd.getVersion(), patch, "newPhoto.jpg", newGenres));
        assertEquals("987654321", rd.getPhoneNumber());
        assertEquals("2012-03-04", rd.getBirthDate().toString());
        assertEquals(newGenres.size(), rd.getInterestList().size());
        assertEquals("newPhoto.jpg", rd.getPhoto().getPhotoFile());
    }

    @Test
    void removePhoto_shouldSetPhotoToNull() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails rd = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"photo.jpg", null);

        assertDoesNotThrow(() -> rd.removePhoto(rd.getVersion()));
        assertNull(rd.getPhoto());
    }
}






