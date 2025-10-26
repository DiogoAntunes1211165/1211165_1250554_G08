package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.*;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.services.google.IsbnLookupService;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource({"classpath:config/library.properties"})
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

	private final BookRepository bookRepository;
	private final GenreRepository genreRepository;
	private final AuthorRepository authorRepository;
	private final PhotoRepository photoRepository;
	private final ReaderRepository readerRepository;

    // Substitui a injeção única por uma lista para suportar múltiplos providers (Google, OpenLibrary, ...)
    private final List<IsbnLookupService> isbnLookupServices;

	@Value("${suggestionsLimitPerGenre}")
	private long suggestionsLimitPerGenre;

	@Override
	public Book create(CreateBookRequest request, String isbn) {

        // Se o ISBN não foi fornecido, tenta buscar pelo título
        if (isbn == null || isbn.isBlank()) {
            // valida título antes do lookup
            if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
                throw new NotFoundException("ISBN not provided and title is empty");
            }
            // tenta obter via providers configurados (por ordem)
            Optional<String> found = findIsbnFromProviders(request.getTitle());
            isbn = found.orElseThrow(() -> new NotFoundException("ISBN not found for title: " + request.getTitle()));
         }

         if(bookRepository.findByIsbn(isbn).isPresent()){
             throw new ConflictException("Book with ISBN " + isbn + " already exists");
         }

		List<Author> authors = new ArrayList<>();
		List<Long> authorNumbers = request.getAuthors();
		if (authorNumbers != null) {
			for (Long authorNumber : authorNumbers) {
				if (authorNumber == null) continue;
				Optional<Author> temp = authorRepository.findByAuthorNumber(authorNumber.toString());
				temp.ifPresent(authors::add);
			}
		}

		MultipartFile photo = request.getPhoto();
		String photoURI = request.getPhotoURI();
		if(photo == null && photoURI != null || photo != null && photoURI == null) {
			request.setPhoto(null);
			request.setPhotoURI(null);
		}

		final var genre = genreRepository.findByString(request.getGenre())
				.orElseThrow(() -> new NotFoundException("Genre not found"));

		Book newBook = new Book(isbn, request.getTitle(), request.getDescription(), genre, authors, photoURI);

        return bookRepository.save(newBook);
	}

    // Método auxiliar que tenta todos os IsbnLookupService em ordem até obter um ISBN.
    private Optional<String> findIsbnFromProviders(String title) {
        if (title == null || title.isBlank()) return Optional.empty();
        if (isbnLookupServices == null || isbnLookupServices.isEmpty()) {
            logger.warn("No IsbnLookupService beans available to query for title: {}", title);
            return Optional.empty();
        }

        for (IsbnLookupService svc : isbnLookupServices) {
            try {
                Optional<String> res = svc.findIsbnByTitle(title);
                if (res.isPresent()) {
                    logger.info("BookService: found ISBN '{}' via provider '{}' for title '{}'", res.get(), svc.getServiceName(), title);
                    return res;
                } else {
                    logger.debug("BookService: provider '{}' returned no result for title '{}'", svc.getServiceName(), title);
                }
            } catch (Exception e) {
                logger.warn("BookService: provider '{}' failed for title '{}': {}", svc.getServiceName(), title, e.getMessage());
            }
        }
        return Optional.empty();
    }

	@Override
	public Book update(UpdateBookRequest request, String currentVersion) {

        var book = findByIsbn(request.getIsbn());
        if(request.getAuthors()!= null) {
            List<Long> authorNumbers = request.getAuthors();
            List<Author> authors = new ArrayList<>();
            for (Long authorNumber : authorNumbers) {
                Optional<Author> temp = authorRepository.findByAuthorNumber(authorNumber.toString());
                if (temp.isEmpty()) {
                    continue;
                }
                Author author = temp.get();
                authors.add(author);
            }

            request.setAuthorObjList(authors);
        }

		MultipartFile photo = request.getPhoto();
		String photoURI = request.getPhotoURI();
		if(photo == null && photoURI != null || photo != null && photoURI == null) {
			request.setPhoto(null);
			request.setPhotoURI(null);
		}

        if (request.getGenre() != null) {
            Optional<Genre> genre = genreRepository.findByString(request.getGenre());
            if (genre.isEmpty()) {
                throw new NotFoundException("Genre not found");
            }
            request.setGenreObj(genre.get());
        }

        book.applyPatch(Long.parseLong(currentVersion), request);

		bookRepository.save(book);


		return book;
	}

	@Override
	public Book save(Book book) {
		return this.bookRepository.save(book);
	}

	@Override
	public List<BookCountDTO> findTop5BooksLent(){
		LocalDate oneYearAgo = LocalDate.now().minusYears(1);
		Pageable pageableRules = PageRequest.of(0,5);
		return this.bookRepository.findTop5BooksLent(oneYearAgo, pageableRules).getContent();
	}

	@Override
	public Book removeBookPhoto(String isbn, long desiredVersion) {
		Book book = this.findByIsbn(isbn);
		String photoFile;
		try {
			photoFile = book.getPhoto().getPhotoFile();
		}catch (NullPointerException e){
			throw new NotFoundException("Book did not have a photo assigned to it.");
		}

		book.removePhoto(desiredVersion);
		var updatedBook = bookRepository.save(book);
		photoRepository.deleteByPhotoFile(photoFile);
		return updatedBook;
	}

	@Override
	public List<Book> findByGenre(String genre) {
		return this.bookRepository.findByGenre(genre);
	}

	public List<Book> findByTitle(String title) {
		return bookRepository.findByTitle(title);
	}

	@Override
	public List<Book> findByAuthorName(String authorName) {
		return bookRepository.findByAuthorName(authorName + "%");
	}

	public Book findByIsbn(String isbn) {
		return this.bookRepository.findByIsbn(isbn)
				.orElseThrow(() -> new NotFoundException(Book.class, isbn));
	}

	public List<Book> getBooksSuggestionsForReader(String readerNumber) {
		List<Book> books = new ArrayList<>();

		ReaderDetails readerDetails = readerRepository.findByReaderNumber(readerNumber)
				.orElseThrow(() -> new NotFoundException("Reader not found with provided login"));
		List<Genre> interestList = readerDetails.getInterestList();

		if(interestList.isEmpty()) {
			throw new NotFoundException("Reader has no interests");
		}

		for(Genre genre : interestList) {
			List<Book> tempBooks = bookRepository.findByGenre(genre.toString());
			if(tempBooks.isEmpty()) {
				continue;
			}

			long genreBookCount = 0;

            for (Book loopBook : tempBooks) {
                if (genreBookCount >= suggestionsLimitPerGenre) {
                    break;
                }

                books.add(loopBook);
				genreBookCount++;
            }
		}

		return books;
	}

	@Override
	public List<Book> searchBooks(Page page, SearchBooksQuery query) {
		if (page == null) {
			page = new Page(1, 10);
		}
		if (query == null) {
			query = new SearchBooksQuery("", "", "");
		}
		return bookRepository.searchBooks(page, query);
	}
}
