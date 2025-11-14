package pt.psoft.g1.psoftg1.unitTests.authormanagement.model;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Photo;


import static org.junit.jupiter.api.Assertions.*;

class AuthorTest {
    private final String validName = "João Alberto";
    private final String validBio = "O João Alberto nasceu em Chaves e foi pedreiro a maior parte da sua vida.";

    private final UpdateAuthorRequest request = new UpdateAuthorRequest(validName, validBio, null, null);

    private static class EntityWithPhotoImpl extends EntityWithPhoto { }
    @BeforeEach
    void setUp() {
    }
    @Test
    void ensureNameNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Author(null,validBio, null, null));
    }

    @Test
    void testGetBio(){
        Author author = new Author(validName, validBio, null, null);
        assertEquals(validBio, author.getBio());
    }

    @Test
    void testGetName(){
        Author author = new Author(validName, validBio, null, null);
        assertEquals(validName, author.getName());
    }





    @Test
    void ensureBioNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Author(validName,null, null, null));
    }

    @Test
    void whenVersionIsStaleItIsNotPossibleToPatch() {
        final var subject = new Author(validName,validBio, null, null);

        assertThrows(StaleObjectStateException.class, () -> subject.applyPatch(999, request));
    }



    @Test
    void testCreateAuthorWithoutPhoto() {
        Author author = new Author(validName, validBio, null, null);
        assertNotNull(author);
        assertNull(author.getPhoto());
    }

    @Test
    void testCreateAuthorRequestWithPhoto() {
        CreateAuthorRequest request = new CreateAuthorRequest(validName, validBio, null, "photoTest.jpg");
        Author author = new Author(request.getName(), request.getBio(), "photoTest.jpg", null);
        assertNotNull(author);
        assertEquals(request.getPhotoURI(), author.getPhoto().getPhotoFile());
    }

    @Test
    void testCreateAuthorRequestWithoutPhoto() {
        CreateAuthorRequest request = new CreateAuthorRequest(validName, validBio, null, null);
        Author author = new Author(request.getName(), request.getBio(), null, null);
        assertNotNull(author);
        assertNull(author.getPhoto());
    }

    @Test
    void testEntityWithPhotoSetPhotoInternalWithValidURI() {
        EntityWithPhoto entity = new EntityWithPhotoImpl();
        String validPhotoURI = "photoTest.jpg";
        entity.setPhoto(validPhotoURI);
        assertNotNull(entity.getPhoto());
    }

    @Test
    void ensurePhotoCanBeNull_AkaOptional() {
        Author author = new Author(validName, validBio, null, null);
        assertNull(author.getPhoto());
    }

    @Test
    void ensureValidPhoto() {
        Author author = new Author(validName, validBio, "photoTest.jpg", null);
        Photo photo = author.getPhoto();
        assertNotNull(photo);
        assertEquals("photoTest.jpg", photo.getPhotoFile());
    }

    @Test
    void testSetGenIdWithNull() {
        Author author = new Author(validName, validBio, null, null);
        assertNotNull(author.getId()); // o construtor já deve gerar genId
    }

    @Test
    void testSetGenIdWithExisting() {
        Author author = new Author(validName, validBio, null, "EXISTING_ID");
        author.setGenId("NEW_ID"); // deve manter o valor passado
        assertEquals("NEW_ID", author.getId());
    }

    @Test
    void testApplyPatchAllFields() {
        Author author = new Author(validName, validBio, null, null);
        UpdateAuthorRequest patch = new UpdateAuthorRequest("Nova Bio", "Novo Nome", null, "newPhoto.jpg");
        author.applyPatch(author.getVersion(), patch);
        assertEquals("Novo Nome", author.getName());
        assertEquals("Nova Bio", author.getBio());
    }

    @Test
    void testRemovePhotoSuccess() {
        Author author = new Author(validName, validBio, "photo.jpg", null);
        long version = author.getVersion();
        author.removePhoto(version);
        assertNull(author.getPhoto());
    }

    @Test
    void testRemovePhotoConflict() {
        Author author = new Author(validName, validBio, "photo.jpg", null);
        long wrongVersion = author.getVersion() + 1;
        assertThrows(ConflictException.class, () -> author.removePhoto(wrongVersion));
    }





}

