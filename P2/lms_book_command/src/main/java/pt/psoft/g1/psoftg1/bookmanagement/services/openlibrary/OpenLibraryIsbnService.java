package pt.psoft.g1.psoftg1.bookmanagement.services.openlibrary;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.IsbnLookupService;

import java.util.Optional;

@Service
@Order(2)
public class OpenLibraryIsbnService implements IsbnLookupService {
    private final OpenLibraryApiClient openLibraryApiClient;

    public OpenLibraryIsbnService(OpenLibraryApiClient openLibraryApiClient) {
        this.openLibraryApiClient = openLibraryApiClient;
    }

    @Override
    public Optional<String> findIsbnByTitle(String title) {
        return openLibraryApiClient.fetchBookByTitle(title);
    }

    @Override
    public String getServiceName() {
        return "OpenLibrary";
    }
}

