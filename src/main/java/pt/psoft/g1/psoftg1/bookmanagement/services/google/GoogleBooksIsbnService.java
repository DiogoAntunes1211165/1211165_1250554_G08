package pt.psoft.g1.psoftg1.bookmanagement.services.google;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.IsbnLookupService;

import java.util.Optional;

@Service
@Order(1)
public class GoogleBooksIsbnService implements IsbnLookupService {
    private final GoogleBooksApiClient googleBooksApiClient;

    public GoogleBooksIsbnService(GoogleBooksApiClient googleBooksApiClient) {
        this.googleBooksApiClient = googleBooksApiClient;
    }

    @Override
    public Optional<String> findIsbnByTitle(String title) {
        return googleBooksApiClient.fetchBookByTitle(title);
    }

    @Override
    public String getServiceName() {
        return "GoogleBooks";
    }
}
