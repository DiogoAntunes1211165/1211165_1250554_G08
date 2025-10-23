package pt.psoft.g1.psoftg1.bookmanagement.services.openlibrary;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.psoft.g1.psoftg1.bookmanagement.services.google.IsbnLookupService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Order(1)
@Profile("openlibrary")
public class OpenLibraryIsbnLookupService implements IsbnLookupService {

    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryIsbnLookupService.class);

    // Template da Open Library Search API (pesquisa por título)
    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?q=%s&limit=10";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> findIsbnByTitle(String title) {
        if (title == null || title.isBlank()) return Optional.empty();

        try {
            // URL de pesquisa
            String searchUrl = "https://openlibrary.org/search.json?title=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            logger.info("OpenLibrary Search URL: {}", searchUrl);

            String resp = restTemplate.getForObject(searchUrl, String.class);
            JsonNode docs = objectMapper.readTree(resp).path("docs");

            if (!docs.isArray() || docs.isEmpty()) return Optional.empty();

            // Extrai workKey
            String workKey = docs.get(0).path("key").asText(null);
            if (workKey == null) return Optional.empty();

            // URL de edições
            String editionsUrl = "https://openlibrary.org" + workKey + "/editions.json?limit=10";
            logger.info("OpenLibrary Editions URL: {}", editionsUrl);

            String editionsResp = restTemplate.getForObject(editionsUrl, String.class);
            JsonNode entries = objectMapper.readTree(editionsResp).path("entries");

            // Procura ISBN
            for (JsonNode edition : entries) {
                JsonNode isbn13 = edition.path("isbn_13");
                if (isbn13.isArray() && !isbn13.isEmpty()) {
                    String isbn = isbn13.get(0).asText();
                    logger.info("Found ISBN: {}", isbn);
                    return Optional.of(isbn);
                }
                JsonNode isbn10 = edition.path("isbn_10");
                if (isbn10.isArray() && !isbn10.isEmpty()) {
                    String isbn = isbn10.get(0).asText();
                    logger.info("Found ISBN: {}", isbn);
                    return Optional.of(isbn);
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.warn("OpenLibrary lookup failed for title '{}': {}", title, e.getMessage());
            return Optional.empty();
        }
    }





    @Override
    public String getServiceName() {
        return "OpenLibrary";
    }
}
