package pt.psoft.g1.psoftg1.bookmanagement.repositories.nonrelational;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.nonrelational.BookDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepositoryMongoDB extends MongoRepository<BookDocument, String> {

    // Pesquisa livros por género (campo interno)
    List<BookDocument> findByGenre_Genre(String genre);

    // Pesquisa livros pelo título (ignora maiúsculas/minúsculas)
    List<BookDocument> findByTitleContainingIgnoreCase(String title);

    // Pesquisa livros pelo nome do autor (campo dentro da lista authors)
    List<BookDocument> findByAuthors_NameContainingIgnoreCase(String name);

    // Pesquisa por ISBN
    @Query( "{ 'isbn.isbn': ?0 }" )
    Optional<BookDocument> findByIsbn(String isbn);

    // Pesquisa por número do autor
    List<BookDocument> findByAuthors_AuthorNumber(String authorNumber);

    // Pesquisa genérica combinada (usada pelo searchBooks)
    @Query("{ '$and': [ "
            + " { '$or': [ "
            + "   { 'title': { '$regex': ?0, '$options': 'i' } }, "
            + "   { 'genre.genre': { '$regex': ?1, '$options': 'i' } }, "
            + "   { 'authors.name': { '$regex': ?2, '$options': 'i' } } "
            + " ] } "
            + "] }")
    List<BookDocument> searchBooks(String titlePattern, String genrePattern, String authorPattern);
}
