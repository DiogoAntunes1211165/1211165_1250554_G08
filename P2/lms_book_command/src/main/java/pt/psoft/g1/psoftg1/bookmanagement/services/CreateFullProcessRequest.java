package pt.psoft.g1.psoftg1.bookmanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;



@Getter
@Data
@NoArgsConstructor
@Schema(description = "A DTO for creating a Book, Genre and Author")
public class CreateFullProcessRequest {

    @Setter
    private String description;

    @NotBlank
    private String title;

    // for the genre
    @NotBlank
    private String genre;

    @Nullable
    @Getter
    @Setter
    private MultipartFile bookPhoto;

    @Nullable
    @Getter
    @Setter
    private String bookPhotoURI;

    // for the author
    @Size(min = 1, max = 150)
    private String name;

    @Size(min = 1, max = 4096)
    private String bio;

    @Nullable
    @Getter
    @Setter
    private MultipartFile authorPhoto;

    @Nullable
    @Getter
    @Setter
    private String authorPhotoURI;
}