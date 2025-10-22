package pt.psoft.g1.psoftg1.bookmanagement.services.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Profile("google")
public class GoogleBooksIsbnLookupService implements IsbnLookupService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksIsbnLookupService.class);
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=intitle:%s&maxResults=5";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> findIsbnByTitle(String title) {
        if (title == null || title.isBlank()) return Optional.empty();

        // Normalize the title to avoid encoding issues
        String normalizedTitle = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFC);
        logger.debug("GoogleBooks lookup called for normalized title='{}'", normalizedTitle);

        // Try to fix common mojibake problems before querying the API (e.g. "Pa├¡s" -> "País").
        String fixedTitle = fixMojibakeIfNeeded(normalizedTitle);
        if (!fixedTitle.equals(normalizedTitle)) {
            logger.debug("GoogleBooks title fixed from '{}' to '{}'", normalizedTitle, fixedTitle);
        }

        try {
            String q = URLEncoder.encode(fixedTitle, StandardCharsets.UTF_8);
            String url = String.format(GOOGLE_BOOKS_API, q);
            String resp = restTemplate.getForObject(url, String.class);
            if (resp == null || resp.isBlank()) {
                logger.debug("GoogleBooks response empty for title='{}'", fixedTitle);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(resp);
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                logger.debug("GoogleBooks no items for title='{}'", fixedTitle);
                return Optional.empty();
            }

            for (JsonNode item : items) {
                JsonNode identifiers = item.path("volumeInfo").path("industryIdentifiers");
                if (identifiers.isArray()) {
                    // prefer ISBN_13 then ISBN_10
                    String isbn10 = null;
                    String isbn13 = null;
                    for (JsonNode idNode : identifiers) {
                        String type = idNode.path("type").asText("");
                        String identifier = idNode.path("identifier").asText("");
                        if ("ISBN_13".equalsIgnoreCase(type)) {
                            isbn13 = identifier;
                            break; // prefer 13
                        } else if ("ISBN_10".equalsIgnoreCase(type)) {
                            isbn10 = identifier;
                        }
                    }
                    if (isbn13 != null && !isbn13.isBlank()) {
                        logger.debug("GoogleBooks found ISBN_13='{}' for title='{}'", isbn13, fixedTitle);
                        return Optional.of(isbn13);
                    }
                    if (isbn10 != null && !isbn10.isBlank()) {
                        logger.debug("GoogleBooks found ISBN_10='{}' for title='{}'", isbn10, fixedTitle);
                        return Optional.of(isbn10);
                    }
                }
            }

            logger.debug("GoogleBooks no ISBN found for title='{}'", fixedTitle);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("GoogleBooks lookup failed for title='{}': {}", fixedTitle, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getServiceName() {
        return "GoogleBooks";
    }

    // Attempt to fix common mojibake where a UTF-8 string was mis-decoded as ISO-8859-1 (or similar).
    // This is defensive: we only try the re-interpretation when suspicious characters are present.
    private String fixMojibakeIfNeeded(String s) {
        if (s == null) return null;
        // Quick heuristic: typical mojibake contains characters like 'Ã', 'Â', or box-drawing chars such as '├', '┤'.
        if (!s.contains("Ã") && !s.contains("Â") && !s.contains("├") && !s.contains("┤") && !s.contains("�")) {
            return s; // looks fine
        }

        try {
            // Interpret the current (wrongly-decoded) Java string as ISO-8859-1 bytes and decode them as UTF-8.
            byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
            String repaired = new String(bytes, StandardCharsets.UTF_8);
            // Basic sanity: repaired should contain only valid printable chars and at least one letter
            if (repaired.chars().anyMatch(Character::isLetter)) {
                return repaired;
            }
        } catch (Exception ex) {
            // ignore and fall through
            logger.debug("Mojibake fix attempt failed for '{}': {}", s, ex.getMessage());
        }

        return s;
    }
}
