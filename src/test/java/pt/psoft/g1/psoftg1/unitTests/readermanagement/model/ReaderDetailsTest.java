package pt.psoft.g1.psoftg1.unitTests.readermanagement.model;

import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReaderDetailsTest {
    @Test
    void ensureValidReaderDetailsAreCreated() {
        Reader mockReader = mock(Reader.class);
        assertDoesNotThrow(() -> new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,null, null));
    }

    @Test
    void ensureExceptionIsThrownForNullReader() {
        assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, null, "2010-01-01", "912345678", true, false, false,null,null));
    }

    @Test
    void ensureExceptionIsThrownForNullPhoneNumber() {
        Reader mockReader = mock(Reader.class);
        assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, mockReader, "2010-01-01", null, true, false, false,null,null));
    }

    @Test
    void ensureExceptionIsThrownForNoGdprConsent() {
        Reader mockReader = mock(Reader.class);
        assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, mockReader, "2010-01-01", "912345678", false, false, false,null,null));
    }

    @Test
    void ensureGdprConsentIsTrue() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,null,null);
        assertTrue(readerDetails.isGdprConsent());
    }

    @Test
    void ensurePhotoCanBeNull_AkaOptional() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,null,null);
        assertNull(readerDetails.getPhoto());
    }

    @Test
    void ensureValidPhoto() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"readerPhotoTest.jpg",null);
        Photo photo = readerDetails.getPhoto();

        //This is here to force the test to fail if the photo is null
        assertNotNull(photo);

        String readerPhoto = photo.getPhotoFile();
        assertEquals("readerPhotoTest.jpg", readerPhoto);
    }

    @Test
    void ensureInterestListCanBeNullOrEmptyList_AkaOptional() {
        Reader mockReader = mock(Reader.class);
        ReaderDetails readerDetailsNullInterestList = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"readerPhotoTest.jpg",null);
        assertNull(readerDetailsNullInterestList.getInterestList());

        ReaderDetails readerDetailsInterestListEmpty = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"readerPhotoTest.jpg",new ArrayList<>());
        assertEquals(0, readerDetailsInterestListEmpty.getInterestList().size());
    }

    @Test
    void ensureInterestListCanTakeAnyValidGenre() {
        Reader mockReader = mock(Reader.class);
        Genre g1 = new Genre("genre1");
        Genre g2 = new Genre("genre2");
        List<Genre> genreList = new ArrayList<>();
        genreList.add(g1);
        genreList.add(g2);

        ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false,"readerPhotoTest.jpg",genreList);
        assertEquals(2, readerDetails.getInterestList().size());
    }

    @Test
    void readerDetails_gettersAndPatch_withMock_shouldWork() {
        // Mock do Reader
        Reader mockReader = mock(Reader.class);

        List<Genre> genres = List.of(new Genre("Fiction"));
        ReaderDetails rd = new ReaderDetails(1, mockReader, "2000-01-01", "912345678", true, false, true, "photo.jpg", genres);

        // Test getters iniciais
        assertNotNull(rd.getReader());
        assertEquals("2000-01-01", rd.getBirthDate().toString());
        assertTrue(rd.isGdprConsent());
        assertFalse(rd.isMarketingConsent());
        assertTrue(rd.isThirdPartySharingConsent());
        assertEquals(genres, rd.getInterestList());
        assertEquals("2025/1", rd.getReaderNumber());
        assertEquals("912345678", rd.getPhoneNumber());
        assertEquals(0L, rd.getVersion());

        // Request para patch
        UpdateReaderRequest req = new UpdateReaderRequest();
        req.setFullName("New Name");
        req.setUsername("newuser");
        req.setPassword("newpass");
        req.setBirthDate("1990-02-02");
        req.setPhoneNumber("987654321");
        req.setMarketing(true);
        req.setThirdParty(false);

        rd.applyPatch(0L, req, "newphoto.jpg", List.of(new Genre("Drama")));

        // Verifica que métodos do mock foram chamados corretamente
        verify(mockReader).setName("New Name");
        verify(mockReader).setUsername("newuser");
        verify(mockReader).setPassword("newpass");

        // Verifica o estado do ReaderDetails (campos que não são mock)
        assertEquals("1990-02-02", rd.getBirthDate().toString());
        assertEquals("987654321", rd.getPhoneNumber());
        assertTrue(rd.isMarketingConsent());
        assertFalse(rd.isThirdPartySharingConsent());
        assertEquals("newphoto.jpg", rd.getPhoto().getPhotoFile());
        assertEquals(1, rd.getInterestList().size());
        assertEquals("Drama", rd.getInterestList().get(0).getGenre());
    }




}
