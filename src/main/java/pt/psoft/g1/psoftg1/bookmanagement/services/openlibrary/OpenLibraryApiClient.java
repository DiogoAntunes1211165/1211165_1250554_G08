package pt.psoft.g1.psoftg1.bookmanagement.services.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component

public class OpenLibraryApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryApiClient.class);

    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?title=%s&limit=5";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> fetchBookByTitle(String title) {
        if (title == null || title.isBlank()) return Optional.empty();

        String queryTitle = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFC);

        try {
            String q = URLEncoder.encode(queryTitle, StandardCharsets.UTF_8);
            String url = String.format(OPEN_LIBRARY_API, q);

            logger.debug("OpenLibrary encoded query='{}', url='{}'", q, url);
            String resp = restTemplate.getForObject(url, String.class);

            if (resp == null || resp.isBlank()) {
                logger.debug("OpenLibrary response empty for title='{}'", queryTitle);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(resp);
            JsonNode docs = root.path("docs");

            if (!docs.isArray() || docs.isEmpty()) return Optional.empty();

            for (JsonNode doc : docs) {
                JsonNode isbns = doc.path("isbn");
                if (isbns.isArray() && !isbns.isEmpty()) {
                    String isbn = isbns.get(0).asText();
                    logger.debug("OpenLibrary found ISBN='{}' for title='{}'", isbn, queryTitle);
                    return Optional.of(isbn);
                }
            }

            logger.debug("OpenLibrary no ISBN found for title='{}'", queryTitle);
            return Optional.empty();

        } catch (IOException e) {
            logger.warn("OpenLibrary lookup failed for title='{}': {}", queryTitle, e.getMessage());
            return Optional.empty();
        }
    }
}
