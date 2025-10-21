package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Serviço de lookup de ISBN utilizando a Google Books API.
 * Permite procurar o ISBN de um livro pelo seu título.
 */

@Service
public class GoogleBooksLookupService implements IsbnLookupService {

    private final RestTemplate restTemplate;

    public GoogleBooksLookupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * Procura o ISBN-13 de um livro pelo seu título usando a Google Books API.
     *
     * @param title O título do livro a procurar
     * @return Optional com o ISBN-13 se encontrado, ou Optional.empty() caso contrário
     */
    @Override
    public Optional<String> findIsbnByTitle(String title) {
        try {
            // Constrói a URL da API Google Books com o parâmetro intitle para procurar pelo título
            // Substitui espaços por '+' para formar uma query válida
            String url = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + title.replace(" ", "+");

            // Faz a chamada HTTP GET à API e deserializa a resposta JSON para o objeto GoogleBooksResponse
            GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);

            // Verifica se a resposta contém resultados (items)
            if (response != null && response.getItems() != null) {
                // Itera sobre cada livro (item) retornado pela API
                for (GoogleBookItem item : response.getItems()) {
                    // Acede aos identificadores da indústria (ISBNs) dentro do volumeInfo
                    // A estrutura JSON é: items[] -> volumeInfo -> industryIdentifiers[]
                    for (GoogleBookIdentifier identifier : item.getVolumeInfo().getIndustryIdentifiers()) {
                        // Procura especificamente por um identificador do tipo ISBN_13
                        // (pode também existir ISBN_10, mas preferimos o ISBN-13)
                        if ("ISBN_13".equals(identifier.getType())) {
                            // Retorna o primeiro ISBN-13 encontrado
                            return Optional.of(identifier.getIdentifier());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Em caso de erro (rede, parsing, etc.), retorna vazio
            // TODO: considerar logging do erro
        }
        // Se não encontrou nenhum ISBN-13, retorna Optional vazio
        return Optional.empty();
    }

    @Override
    public String getServiceName() {
        return "GoogleBooks";
    }
}
