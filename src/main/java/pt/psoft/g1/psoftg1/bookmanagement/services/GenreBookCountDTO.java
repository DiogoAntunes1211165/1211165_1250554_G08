package pt.psoft.g1.psoftg1.bookmanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreBookCountDTO implements Serializable {
    private String genre;
    private long bookCount;
}
