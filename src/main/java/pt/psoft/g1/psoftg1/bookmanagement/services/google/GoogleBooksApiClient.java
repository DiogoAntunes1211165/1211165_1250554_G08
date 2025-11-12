package pt.psoft.g1.psoftg1.bookmanagement.services.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class GoogleBooksApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksApiClient.class);

    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=intitle:%s&maxResults=10";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> fetchBookByTitle(String title) {
        if (title == null || title.isBlank()) return Optional.empty();

        String queryTitle = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFC);

        try {
            // Codifica a query para uso em URL (UTF-8 percent-encoding).
            // Ex.: espaços -> %20, 'ç' -> %C3%A7, etc.
            String q = URLEncoder.encode(queryTitle, StandardCharsets.UTF_8);

            // Constroi a URL final usando o template definido acima.
            String url = String.format(GOOGLE_BOOKS_API, q);

            // Log para depuração: vemos a query codificada e a URL completa que será chamada.
            logger.debug("GoogleBooks encoded query='{}', url='{}'", q, url);

            // Chamada HTTP GET para a Google Books API; o resultado é um JSON como String.
            String resp = restTemplate.getForObject(url, String.class);

            // Se a resposta for vazia ou nula, regista e devolve vazio.
            if (resp == null || resp.isBlank()) {
                logger.debug("GoogleBooks response empty for title='{}'", queryTitle);
                return Optional.empty();
            }

            // Lê o JSON retornado para uma árvore de JsonNode.
            JsonNode root = objectMapper.readTree(resp);

            // A API normalmente devolve um array `items` com os resultados.
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                // Se não houver items, não encontramos correspondências.
                logger.debug("GoogleBooks no items for title='{}'", queryTitle);
                return Optional.empty();
            }

            // Percorre os items (aqui maxResults=1, mas o código lida com vários caso se altere).
            for (JsonNode item : items) {
                // Dentro de cada item procuramos `volumeInfo.industryIdentifiers` que contém ISBNs.
                JsonNode identifiers = item.path("volumeInfo").path("industryIdentifiers");
                if (identifiers.isArray()) {
                    // Preferimos ISBN-13 quando disponível (melhor e mais moderno que ISBN-10).
                    String isbn10 = null;
                    String isbn13 = null;

                    // Percorre os identificadores e extrai tipo e valor.
                    for (JsonNode idNode : identifiers) {
                        String type = idNode.path("type").asText("");
                        String identifier = idNode.path("identifier").asText("");

                        // Guarda o ISBN adequado conforme o tipo (ISBN_13 preferencialmente).
                        if ("ISBN_13".equalsIgnoreCase(type)) {
                            isbn13 = identifier;
                            break; // se encontramos 13, paramos — é prioridade
                        } else if ("ISBN_10".equalsIgnoreCase(type)) {
                            isbn10 = identifier;
                        }
                    }

                    // Se encontramos ISBN-13 válido, retorna-o.
                    if (isbn13 != null && !isbn13.isBlank()) {
                        logger.debug("GoogleBooks found ISBN_13='{}' for title='{}'", isbn13, queryTitle);
                        return Optional.of(isbn13);
                    }

                    // Caso contrário, se houver ISBN-10, retorna-o.
                    if (isbn10 != null && !isbn10.isBlank()) {
                        logger.debug("GoogleBooks found ISBN_10='{}' for title='{}'", isbn10, queryTitle);
                        return Optional.of(isbn10);
                    }
                }

                // Se chegamos aqui para este item, não havia identificadores válidos — continua para o próximo.
            }

            // Se nenhum item continha ISBNs, devolve vazio.
            logger.debug("GoogleBooks no ISBN found for title='{}'", queryTitle);
            return Optional.empty();
        } catch (IOException e) {
            // Captura erros de parsing do JSON e regista aviso; devolve Optional vazio em caso de falha.
            logger.warn("GoogleBooks lookup failed for title='{}': {}", queryTitle, e.getMessage());
            return Optional.empty();
        }
    }

}
