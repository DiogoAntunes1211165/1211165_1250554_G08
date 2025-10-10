package pt.psoft.g1.psoftg1.bookmanagement.repositories.mappers;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.relational.TitleEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.relational.GenreEntity;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.relational.PhotoEntity;

public interface BookEntityMapper {

    Book toDomain(BookEntity bookEntity);

    BookEntity toEntity(Book book);


    default String map(Genre value){
        if (value == null){
            return null;
        }
        return value.getGenre(); // Exemplo para Genre
    }

    default String map(GenreEntity value){
        if (value == null){
            return null;
        }
        return value.getGenre(); // Exemplo para Genre
    }

    default String map(Photo photo) {
        if (photo == null) {
            return null;
        }
        return photo.getPhotoFile();
    }

    default String map(PhotoEntity photoEntity) {
        if (photoEntity == null) {
            return null;
        }
        return photoEntity.getPhotoFile();
    }

    default String map(TitleEntity value) {
        if (value == null) {
            return null;
        }
        return value.getTitle(); // Exemplo para TitleEntity
    }

    default String map(Title value) {
        if (value == null) {
            return null;
        }
        return value.getTitle();  // Exemplo para Title
    }



}
