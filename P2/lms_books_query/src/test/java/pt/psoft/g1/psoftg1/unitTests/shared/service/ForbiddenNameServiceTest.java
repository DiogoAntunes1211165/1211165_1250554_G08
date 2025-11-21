package pt.psoft.g1.psoftg1.unitTests.shared.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ClassPathResource;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameServiceImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForbiddenNameServiceTest {

    @Mock
    private ForbiddenNameRepository forbiddenNameRepository;

    @InjectMocks
    private ForbiddenNameServiceImpl forbiddenNameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void loadDataFromFile_shouldSaveNewNames_whenNotInRepository() throws Exception {
        // Arrange
        String fakeFile = "test_forbidden_names.txt";
        String fileContent = "badword1\nbadword2\n";

        // Mock do ClassPathResource para devolver um InputStream personalizado
        ClassPathResource resource = mock(ClassPathResource.class);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

        // Mock static do construtor do ClassPathResource (simula o comportamento real)
        try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
                (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8))))) {

            when(forbiddenNameRepository.findByForbiddenName("badword1")).thenReturn(Optional.empty());
            when(forbiddenNameRepository.findByForbiddenName("badword2")).thenReturn(Optional.empty());

            // Act
            forbiddenNameService.loadDataFromFile(fakeFile);


        }
    }


    @Test
    void loadDataFromFile_shouldNotSave_whenNameAlreadyExists() throws Exception {
        String fakeFile = "existing_name.txt";
        String fileContent = "duplicate\n";

        try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
                (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8))))) {

            when(forbiddenNameRepository.findByForbiddenName("duplicate"))
                    .thenReturn(Optional.of(new ForbiddenName("duplicate")));

            // Act
            forbiddenNameService.loadDataFromFile(fakeFile);

            // Assert
            verify(forbiddenNameRepository, never()).save(any());
        }
    }


    @Test
    void loadDataFromFile_shouldThrowRuntimeException_whenIOExceptionOccurs() throws Exception {
        String fakeFile = "missing.txt";

        try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
                (mock, context) -> when(mock.getInputStream()).thenThrow(new IOException("File not found")))) {

            // Act + Assert
            assertThrows(RuntimeException.class, () -> forbiddenNameService.loadDataFromFile(fakeFile));
        }
    }
}
