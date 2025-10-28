package pt.psoft.g1.psoftg1.bootstrapping;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.google.IsbnLookupService;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
@PropertySource({"classpath:config/library.properties"})
@Order(2)
public class Bootstrapper implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Bootstrapper.class);


    private static final Map<String, String> FALLBACK_ISBNS = Map.ofEntries(
            Map.entry("O País das Pessoas de Pernas Para o Ar", "9789720014979"),
            Map.entry("Como se Desenha Uma Casa", "9789720706386"),
            Map.entry("C e Algoritmos", "9789723716160"),
            Map.entry("Introdução ao Desenvolvimento Moderno para a Web", "9789895612864"),
            Map.entry("O Principezinho", "9782722203402"),
            Map.entry("A Criada Está a Ver", "9789722328296"),
            Map.entry("O Hobbit", "9789895702756"),
            Map.entry("Histórias de Vigaristas e Canalhas", "9789897776090"),
            Map.entry("Histórias de Aventureiros e Patifes", "9789896379636"),
            Map.entry("Windhaven", "9789896378905")
    );

    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays;
    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents;

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LendingRepository lendingRepository;
    private final ReaderRepository readerRepository;

    private final ForbiddenNameService forbiddenNameService;

    private final List<IsbnLookupService> isbnLookupServices;

    @Override
    @Transactional
    public void run(final String... args) {
        logger.info("Bootstrapping data...");

        createAuthors();
        createGenres();
        createBooks();


        loadForbiddenNames();
        createLendings();
        /* createPhotos(); */

        logger.info("Bootstrapping data complete.");
    }

    private void createAuthors() {
        if (authorRepository.searchByNameName("Manuel Antonio Pina").isEmpty()) {
            final Author author = new Author("Manuel Antonio Pina",
                    "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Antoine de Saint Exupéry").isEmpty()) {
            final Author author = new Author("Antoine de Saint Exupéry",
                    "Antoine de Saint-Exupéry nasceu a 29 de junho de 1900 em Lyon. Faz o seu batismo de voo aos 12 anos, aos 22 torna-se piloto militar e é como capitão que em 1939 se junta à Força Aérea francesa em luta contra a ocupação nazi. A aviação e a guerra viriam a revelar-se elementos centrais de toda a sua obra literária, onde se destacam títulos como Correio do Sul (1929), o seu primeiro romance, Voo Noturno (1931), que logo se tornou um êxito de vendas internacional, e Piloto de Guerra (1942), retrato da sua participação na Segunda Guerra Mundial. Em 1943 publicaria aquela que é reconhecida como a sua obra-prima, O Principezinho, um dos livros mais traduzidos em todo o mundo. A sua morte, aos 44 anos, num acidente de aviação durante uma missão de reconhecimento no sul de França, permanece ainda hoje um mistério.",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Alexandre Pereira").isEmpty()) {
            final Author author = new Author("Alexandre Pereira",
                    "Alexandre Pereira é licenciado e mestre em Engenharia Electrotécnica e de Computadores, pelo Instituto Superior Técnico. É, também, licenciado em Antropologia, pela Faculdade de Ciências Sociais e Humanas da Universidade Nova de Lisboa.\n" +
                            "É Professor Auxiliar Convidado na Universidade Lusófona de Humanidades e Tecnologias, desde Março de 1993, onde lecciona diversas disciplinas na Licenciatura de Informática e lecciona uma cadeira de introdução ao SPSS na Licenciatura de Psicologia.\n" +
                            "Tem também leccionado cursos de formação na área da aplicação da informática ao cálculo estatístico e processamento de dados utilizando o SPSS, em diversas instituições, nomeadamente no Instituto Nacional de Estatística.\n" +
                            "Para além disso, desenvolve aplicações informáticas na área da Psicologia Cognitiva, no âmbito de projectos de investigação do departamento de Psicologia Cognitiva da Faculdade de Psicologia da Universidade de Lisboa.\n" +
                            "Está ainda ligado a projectos de ensino à distância desenvolvidos na Faculdade de Motricidade Humana da Universidade Técnica de Lisboa.\n" +
                            "Paralelamente, tem desenvolvido aplicações de software comercial, área onde continua em actividade. ",
                    null, null);
            authorRepository.save(author);
            System.out.println("Author Alexandre Pereira created.");
        }
        if (authorRepository.searchByNameName("Filipe Portela").isEmpty()) {
            final Author author = new Author("Filipe Portela",
                    " «Docente convidado na Escola de Engenharia da Universidade do Minho. Investigador integrado do Centro Algoritmi. CEO e fundador da startup tecnológica IOTech - Innovation on Technology. Coautor do livro Introdução ao Desenvolvimento Moderno para a Web. ",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Ricardo Queirós").isEmpty()) {
            final Author author = new Author("Ricardo Queirós",
                    "Docente na Escola Superior de Media Artes e Design do Politécnico do Porto. Diretor da uniMAD (ESMAD) e membro efetivo do CRACS (INESC TEC). Autor de vários livros sobre tecnologias Web e programação móvel, publicados pela FCA. Coautor do livro Introdução ao Desenvolvimento Moderno para a Web.",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Freida Mcfadden").isEmpty()) {
            final Author author = new Author("Freida Mcfadden",
                    "Freida McFadden é médica e especialista em lesões cerebrais. Autora de diversos thrillers psicológicos, todos eles bestsellers, já traduzidos para mais de 30 idiomas. As suas obras foram selecionadas para «O Melhor Livro do Ano» na Amazon e também para «Melhor Thriller» dos Goodreads Choice Awards.\n" +
                            "Freida vive com a sua família e o gato preto numa casa de três andares com vista para o oceano, com escadas que rangem e gemem a cada passo, e ninguém conseguiria ouvi-la se gritasse. A menos que gritasse muito alto, talvez.",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("J R R Tolkien").isEmpty()) {
            final Author author = new Author("J R R Tolkien",
                    "J.R.R. Tolkien nasceu a 3 de Janeiro de 1892, em Bloemfontein.\n" +
                            "Depois de ter combatido na Primeira Guerra Mundial, dedicou-se a uma ilustre carreira académica e foi reconhecido como um dos grandes filólogos do planeta.\n" +
                            "Foi a criação da Terra Média, porém, a trazer-lhe a celebridade. Autor de extraordinários clássicos da ficção, de que são exemplo O Hobbit, O Senhor dos Anéis e O Silmarillion, os seus livros foram traduzidos em mais de 60 línguas e venderam largos milhões de exemplares no mundo inteiro.\n" +
                            "Tolkien foi nomeado Comandante da Ordem do Império Britânico e, em 1972, foi-lhe atribuído o título de Doutor Honoris Causa, pela Universidade de Oxford.\n" +
                            "Morreu em 1973, com 81 anos.",
                    "authorPhotoTest.jpg", null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Gardner Dozois").isEmpty()) {
            final Author author = new Author("Gardner Dozois",
                    "Gardner Raymond Dozois (23 de julho de 1947 – 27 de maio de 2018) foi um autor de ficção científica norte-americano.\n" +
                            "Foi o fundador e editor do Melhores Do Ano de Ficção científica antologias (1984–2018) e foi editor da revista Asimov Ficção científica (1984-2004), ganhando vários prémios.",
                    null, null);
            authorRepository.save(author);
        }
        if (authorRepository.searchByNameName("Lisa Tuttle").isEmpty()) {
            final Author author = new Author("Lisa Tuttle",
                    "Lisa Gracia Tuttle (nascida a 16 de setembro de 1952) é uma autora americana de ficção científica, fantasia e terror. Publicou mais de uma dúzia de romances, sete coleções de contos e vários títulos de não-ficção, incluindo um livro de referência sobre feminismo, \"Enciclopédia do Feminismo\" (1986). Também editou várias antologias e fez críticas de livros para diversas publicações. Vive no Reino Unido desde 1981.\n" +
                            "Tuttle ganhou o Prémio John W. Campbell para Melhor Novo Escritor em 1974, recebeu o Prémio Nebula de Melhor Conto em 1982 por \"The Bone Flute\", que recusou, e o Prémio BSFA de Ficção Curta em 1989 por \"In Translation\".",
                    null, null);
            authorRepository.save(author);
        }
    }

    private void createGenres() {
        if (genreRepository.findByString("Fantasia").isEmpty()) {
            final Genre g1 = new Genre("Fantasia");
            genreRepository.save(g1);
        }
        if (genreRepository.findByString("Informação").isEmpty()) {
            final Genre g2 = new Genre("Informação");
            genreRepository.save(g2);
        }
        if (genreRepository.findByString("Romance").isEmpty()) {
            final Genre g3 = new Genre("Romance");
            genreRepository.save(g3);
        }
        if (genreRepository.findByString("Infantil").isEmpty()) {
            final Genre g4 = new Genre("Infantil");
            genreRepository.save(g4);
        }
        if (genreRepository.findByString("Thriller").isEmpty()) {
            final Genre g5 = new Genre("Thriller");
            genreRepository.save(g5);
        }
    }

    // small record to carry ISBN and the service that provided it
    private static record IsbnResult(String isbn, String service) {}

    protected void createBooks() {
        // Início do método createBooks: garante que um conjunto mínimo de livros existe na BD.
        // Recupera o género 'Infantil' (pode ser usado por vários livros) ou lança exceção se não existir.
        Optional<Genre> genre = Optional.ofNullable(genreRepository.findByString("Infantil"))
                .orElseThrow(() -> new NotFoundException("Cannot find genre"));
        // Pesquisa autores pelo nome "Manuel Antonio Pina" e guarda a lista (pode estar vazia).
        List<Author> author = authorRepository.searchByNameName("Manuel Antonio Pina");

        // Função auxiliar: tenta obter um ISBN usando a lista de serviços em ordem; se falhar usa o fallback passado.
        java.util.function.BiFunction<String, String, String> resolveIsbn = (t, fallback) -> {
            Optional<IsbnResult> apiIsbn = tryFindIsbn(t); // agora retorna ISBN + service
            if (apiIsbn.isPresent()) { // Se obteve um ISBN válido, usa-o e loga o serviço
                logger.info("Título: {} , ISBN from API: {} (service={})", t, apiIsbn.get().isbn(), apiIsbn.get().service());
                return apiIsbn.get().isbn();
            } else {
                logger.info("Título: {} , ISBN fallback used: {}", t, fallback);
                return fallback;
            }
        };

        // 1 - título do primeiro livro
        // Define a string do título que vamos criar/procurar.
        String title1 = "O País das Pessoas de Pernas Para o Ar";
        // Resolve o ISBN para o título (usa serviços externos ou fallback se não houver resultado).
        String isbn1 = resolveIsbn.apply(title1, "9789720014979");
        // Verifica se já existe um livro com esse ISBN; se não existir cria-o.
        if (bookRepository.findByIsbn(isbn1).isEmpty()) {
            // Prepara a lista de autores para o livro atual.
            List<Author> authors = new ArrayList<>();
            // Se o género está presente e existe pelo menos um autor encontrado, associa o primeiro autor.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o autor principal à lista de autores do livro.
                authors.add(author.get(0));
                // Cria o objeto Book com ISBN, título, descrição curta, género, autores e sem foto.
                Book book = new Book(isbn1, title1, "Num país muito, muito distante, as pessoas andam de pernas para o ar...", genre.get(), authors, null);
                // Persiste o livro no repositório (base de dados).
                bookRepository.save(book);
            }
        }

        // 2 - título do segundo livro
        // Define o título "Como se Desenha Uma Casa".
        String title2 = "Como se Desenha Uma Casa";
        // Resolve ISBN para o título 2 com fallback específico.
        String isbn2 = resolveIsbn.apply(title2, "9789720706386");
        // Se não existir livro com isbn2, cria-o.
        if (bookRepository.findByIsbn(isbn2).isEmpty()) {
            // Nova lista de autores para este livro.
            List<Author> authors = new ArrayList<>();
            // Se género e autor principal existem, cria e salva o livro.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o autor principal à lista.
                authors.add(author.get(0));
                // Cria o objeto Book com dados do livro 2.
                Book book = new Book(isbn2, title2, "Desenhar uma casa não é assim tão fácil como parece...", genre.get(), authors, null);
                // Persiste o livro 2.
                bookRepository.save(book);
            }
        }

        // 3 - título do terceiro livro
        // Define o título "C e Algoritmos".
        String title3 = "C e Algoritmos";
        // Resolve o ISBN para o título 3 com fallback.
        String isbn3 = resolveIsbn.apply(title3, "9789723716160");
        // Se não existir livro com isbn3, cria-o.
        if (bookRepository.findByIsbn(isbn3).isEmpty()) {
            // Lista de autores local para o livro 3.
            List<Author> authors = new ArrayList<>();
            // Para este livro alteramos o género para "Informação" (específico deste título).
            genre = Optional.ofNullable(genreRepository.findByString("Informação"))
                    // Se o género não existir lançamos NotFoundException.
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procuramos o(s) autor(es) específico(s) para este título.
            author = authorRepository.searchByNameName("Alexandre Pereira");
            // Se género e autores existem, cria e salva o Book.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o primeiro autor encontrado para este livro.
                authors.add(author.get(0));
                // Cria o objeto Book para "C e Algoritmos" com descrição curta.
                Book book = new Book(isbn3, title3, "O C é uma linguagem de programação...", genre.get(), authors, null);
                // Persiste o livro na BD.
                bookRepository.save(book);
            }
        }

        // 4 - título do quarto livro
        // Define o título e resolve ISBN com fallback.
        String title4 = "Introdução ao Desenvolvimento Moderno para a Web";
        String isbn4 = resolveIsbn.apply(title4, "9789895612864");
        // Se não existir livro com isbn4, cria-o.
        if (bookRepository.findByIsbn(isbn4).isEmpty()) {
            // Lista de autores temporária.
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Informação" existe e obtém autores específicos.
            genre = Optional.ofNullable(genreRepository.findByString("Informação"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            author = authorRepository.searchByNameName("Filipe Portela");
            List<Author> author2 = authorRepository.searchByNameName("Ricardo Queirós");
            // Se género e ambos autores estiverem presentes, adiciona-os e persiste o livro.
            if (genre.isPresent() && !author.isEmpty() && !author2.isEmpty()) {
                // Adiciona os dois autores ao livro.
                authors.add(author.get(0));
                authors.add(author2.get(0));
                // Cria o Book com título, descrição curta e os autores.
                Book book = new Book(isbn4, title4, "Este livro foca o desenvolvimento moderno de aplicações Web...", genre.get(), authors, null);
                // Persiste o livro.
                bookRepository.save(book);
            }
        }

        // 5 - O Principezinho
        // Define título e resolve ISBN com fallback.
        String title5 = "O Principezinho";
        String isbn5 = resolveIsbn.apply(title5, "9782722203402");
        // Se não existir livro com isbn5, cria-o.
        if (bookRepository.findByIsbn(isbn5).isEmpty()) {
            // Lista de autores local.
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Infantil" existe para este livro.
            genre = Optional.ofNullable(genreRepository.findByString("Infantil"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autor Antoine de Saint Exupéry.
            author = authorRepository.searchByNameName("Antoine de Saint Exupéry");
            // Se género e autor estiverem presentes, constrói e persiste o Book.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o autor ao livro.
                authors.add(author.get(0));
                // Cria Book com foto associada (bookPhotoTest.jpg) como exemplo.
                Book book = new Book(isbn5, title5, "Depois de deixar o seu asteroide...", genre.get(), authors, "bookPhotoTest.jpg");
                // Persiste o livro na BD.
                bookRepository.save(book);
            }
        }

        // 6 - A Criada Está a Ver
        // Define título e resolve ISBN com fallback.
        String title6 = "A Criada Está a Ver";
        String isbn6 = resolveIsbn.apply(title6, "9789722328296");
        // Se não existir livro com isbn6, cria-o.
        if (bookRepository.findByIsbn(isbn6).isEmpty()) {
            // Lista de autores para este livro.
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Thriller" existe.
            genre = Optional.ofNullable(genreRepository.findByString("Thriller"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autor Freida Mcfadden.
            author = authorRepository.searchByNameName("Freida Mcfadden");
            // Se género e autor existem, cria e persiste o Book.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o autor à lista.
                authors.add(author.get(0));
                // Cria Book com descrição curta.
                Book book = new Book(isbn6, title6, "A Sra. Lowell transborda simpatia...", genre.get(), authors, null);
                // Persiste o livro.
                bookRepository.save(book);
            }
        }

        // 7 - O Hobbit
        // Define título e resolve ISBN com fallback.
        String title7 = "O Hobbit";
        String isbn7 = resolveIsbn.apply(title7, "9789895702756");
        // Se não existir livro com isbn7, cria-o.
        if (bookRepository.findByIsbn(isbn7).isEmpty()) {
            // Lista de autores para O Hobbit.
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Fantasia" existe.
            genre = Optional.ofNullable(genreRepository.findByString("Fantasia"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autor J R R Tolkien.
            author = authorRepository.searchByNameName("J R R Tolkien");
            // Se género e autor estão presentes, cria o Book.
            if (genre.isPresent() && !author.isEmpty()) {
                // Adiciona o autor à lista de autores.
                authors.add(author.get(0));
                // Cria o Book com uma descrição curta.
                Book book = new Book(isbn7, title7, "Esta é a história de como um Baggins viveu uma aventura...", genre.get(), authors, null);
                // Persiste o livro.
                bookRepository.save(book);
            }
        }

        // 8 - Histórias de Vigaristas e Canalhas
        // Define título e resolve ISBN com fallback.
        String title8 = "Histórias de Vigaristas e Canalhas";
        String isbn8 = resolveIsbn.apply(title8, "9789897776090");
        // Se não existir livro com isbn8, cria-o.
        if (bookRepository.findByIsbn(isbn8).isEmpty()) {
            // Lista de autores para este título (podem ser vários).
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Fantasia" existe.
            genre = Optional.ofNullable(genreRepository.findByString("Fantasia"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autores relevantes para a antologia.
            author = authorRepository.searchByNameName("J R R Tolkien");
            List<Author> author2 = authorRepository.searchByNameName("Gardner Dozois");
            // Se género e ambos os autores estiverem presentes, cria e persiste o Book.
            if (genre.isPresent() && !author.isEmpty() && !author2.isEmpty()) {
                // Adiciona os autores encontrados à lista.
                authors.add(author.get(0));
                authors.add(author2.get(0));
                // Cria o Book com descrição curta.
                Book book = new Book(isbn8, title8, "Recomendamos cautela ao ler estes contos...", genre.get(), authors, null);
                // Persiste o livro.
                bookRepository.save(book);
            }
        }

        // 9 - Histórias de Aventureiros e Patifes
        // Define título e resolve ISBN com fallback.
        String title9 = "Histórias de Aventureiros e Patifes";
        String isbn9 = resolveIsbn.apply(title9, "9789896379636");
        // Se não existir livro com isbn9, cria-o.
        if (bookRepository.findByIsbn(isbn9).isEmpty()) {
            // Lista de autores.
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Fantasia" existe.
            genre = Optional.ofNullable(genreRepository.findByString("Fantasia"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autores relevantes.
            author = authorRepository.searchByNameName("J R R Tolkien");
            List<Author> author2 = authorRepository.searchByNameName("Gardner Dozois");
            // Se género e autores existem, adiciona-os e persiste o Book.
            if (genre.isPresent() && !author.isEmpty() && !author2.isEmpty()) {
                authors.add(author.get(0));
                authors.add(author2.get(0));
                Book book = new Book(isbn9, title9, "Recomendamos cautela a ler estes contos...", genre.get(), authors, null);
                bookRepository.save(book);
            }
        }

        // 10 - Windhaven
        // Define título e resolve ISBN com fallback.
        String title10 = "Windhaven";
        String isbn10 = resolveIsbn.apply(title10, "9789896378905");
        // Se não existir livro com isbn10, cria-o.
        if (bookRepository.findByIsbn(isbn10).isEmpty()) {
            // Lista de autores para Windhaven (colaboração entre autores).
            List<Author> authors = new ArrayList<>();
            // Garante que o género "Fantasia" existe.
            genre = Optional.ofNullable(genreRepository.findByString("Fantasia"))
                    .orElseThrow(() -> new NotFoundException("Cannot find genre"));
            // Procura autores J R R Tolkien e Lisa Tuttle.
            author = authorRepository.searchByNameName("J R R Tolkien");
            List<Author> author2 = authorRepository.searchByNameName("Lisa Tuttle");
            // Se género e ambos autores estiverem presentes, adiciona-os e persiste o Book.
            if (genre.isPresent() && !author.isEmpty() && !author2.isEmpty()) {
                authors.add(author.get(0));
                authors.add(author2.get(0));
                Book book = new Book(isbn10, title10, "Ao descobrirem neste novo planeta...", genre.get(), authors, null);
                bookRepository.save(book);
            }
        }
    }

    // Método auxiliar que itera pela lista de IsbnLookupService na ordem injetada e retorna o primeiro ISBN encontrado e o nome do serviço.
    private Optional<IsbnResult> tryFindIsbn(String title) {
        if (title == null || title.isBlank()) return Optional.empty();

        if (isbnLookupServices == null || isbnLookupServices.isEmpty()) {
            logger.warn("No IsbnLookupService beans available to query for title: {}", title);
            return Optional.empty();
        }

        logger.info("Configured IsbnLookupServices: {}", isbnLookupServices.stream().map(IsbnLookupService::getServiceName).toList());

        for (IsbnLookupService svc : isbnLookupServices) {
            try {
                Optional<String> res = svc.findIsbnByTitle(title);
                if (res.isPresent()) {
                    logger.info("Found ISBN from service '{}' for title '{}': {}", svc.getServiceName(), title, res.get());
                    return Optional.of(new IsbnResult(res.get(), svc.getServiceName()));
                } else {
                    logger.debug("Service '{}' returned no result for title '{}'", svc.getServiceName(), title);
                }
            } catch (Exception e) {
                logger.warn("IsbnLookupService '{}' failed for title '{}': {}", svc.getServiceName(), title, e.getMessage());
            }
        }
        return Optional.empty();
    }

    protected void loadForbiddenNames() {
        String fileName = "forbiddenNames.txt";
        forbiddenNameService.loadDataFromFile(fileName);
    }

    private void createLendings() {
        int i;
        int seq = 0;
        // Instead of relying on hard-coded ISBNs being present, try to find the books by title first.
        // If not present by title, ask the external ISBN lookup services (in order) for an ISBN and try to find by that.
        // If any required book is still missing, fail fast with a clear message.
        List<String> requiredTitles = List.of(
                "O País das Pessoas de Pernas Para o Ar",
                "Como se Desenha Uma Casa",
                "C e Algoritmos",
                "Introdução ao Desenvolvimento Moderno para a Web",
                "O Principezinho",
                "A Criada Está a Ver",
                "O Hobbit",
                "Histórias de Vigaristas e Canalhas",
                "Histórias de Aventureiros e Patifes",
                "Windhaven"
        );

        List<Book> books = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        // verifica se cada titula da lista requiredTitles está presente no bookRepository
        for (String title : requiredTitles) {
            List<Book> foundByTitle = bookRepository.findByTitle(title);
            if (foundByTitle != null && !foundByTitle.isEmpty()) {
                books.add(foundByTitle.get(0));
                continue;
            }

            // Se não encontrado pelo título, tenta um ISBN de fallback conhecido
            String fallbackIsbn = FALLBACK_ISBNS.get(title);
            if (fallbackIsbn != null) {
                Optional<Book> foundByFallback = bookRepository.findByIsbn(fallbackIsbn);
                if (foundByFallback.isPresent()) {
                    books.add(foundByFallback.get());
                    continue;
                }
            }

            // 2) tenta obter ISBN via serviços externos
            Optional<IsbnResult> isbnOpt = tryFindIsbn(title);
            if (isbnOpt.isPresent()) {
                // log which service provided the ISBN
                logger.info("Lookup provided ISBN '{}' via service='{}' for title='{}'", isbnOpt.get().isbn(), isbnOpt.get().service(), title);
                Optional<Book> foundByIsbn = bookRepository.findByIsbn(isbnOpt.get().isbn());
                if (foundByIsbn.isPresent()) {
                    books.add(foundByIsbn.get());
                    continue;
                } else {
                    // If the API returned an ISBN that isn't in the DB, try the fallback ISBN as a last resort.
                    if (fallbackIsbn != null) {
                        logger.debug("ISBN '{}' not found in DB; trying fallback ISBN '{}' for title '{}'", isbnOpt.get().isbn(), fallbackIsbn, title);
                        Optional<Book> foundByFallback2 = bookRepository.findByIsbn(fallbackIsbn);
                        if (foundByFallback2.isPresent()) {
                            logger.info("Found book for title '{}' using fallback ISBN '{}'", title, fallbackIsbn);
                            books.add(foundByFallback2.get());
                            continue;
                        }
                    }
                }
            }

            // 3) still not found -> mark missing
            missing.add(title);
        }

        if (!missing.isEmpty()) { // se algum livro obrigatório está em falta, lança exceção
            throw new IllegalStateException("Required books not found in database (and ISBN lookup failed or book not stored): " + missing); //
        }

        final var readerDetails1 = readerRepository.findByReaderNumber("2025/1");
        final var readerDetails2 = readerRepository.findByReaderNumber("2025/2");
        final var readerDetails3 = readerRepository.findByReaderNumber("2025/3");
        final var readerDetails4 = readerRepository.findByReaderNumber("2025/4");
        final var readerDetails5 = readerRepository.findByReaderNumber("2025/5");
        final var readerDetails6 = readerRepository.findByReaderNumber("2025/6");

        List<ReaderDetails> readers = new ArrayList<>();
        // require all six readers to be present before proceeding
        if(readerDetails1.isPresent() && readerDetails2.isPresent() && readerDetails3.isPresent()
                && readerDetails4.isPresent() && readerDetails5.isPresent() && readerDetails6.isPresent()){
            readers = List.of(readerDetails1.get(), readerDetails2.get(), readerDetails3.get(),
                    readerDetails4.get(), readerDetails5.get(), readerDetails6.get());
        }

        if (books.isEmpty() || readers.isEmpty()) {
            throw new IllegalStateException("Livros ou leitores não encontrados na base de dados.");
        }

        LocalDate startDate;
        LocalDate returnedDate;
        Lending lending;


        //Lendings 1 through 3 (late, returned)
        for(i = 0; i < 3; i++){
            ++seq;
            System.out.println("Verifying lending number: " + "2025/" + seq);
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){  // verifica se o lending_number já existe

                startDate = LocalDate.of(2024, 1,31-i);
                returnedDate = LocalDate.of(2024,2,15+i);
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i*2), 2025, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                System.out.println("tou aqui2");
                lendingRepository.save(lending);

            }
        }

        //Lendings 4 through 6 (overdue, not returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 3,25+i);
                lending = Lending.newBootstrappingLending(books.get(1+i), readers.get(1+i*2), 2025, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }
        //Lendings 7 through 9 (late, overdue, not returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 4,(1+2*i));
                lending = Lending.newBootstrappingLending(books.get(3/(i+1)), readers.get(i*2), 2025, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 10 through 12 (returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+1));
                returnedDate = LocalDate.of(2024,5,(i+2));
                lending = Lending.newBootstrappingLending(books.get(3-i), readers.get(1+i*2), 2025, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 13 through 18 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+2));
                returnedDate = LocalDate.of(2024,5,(i+2*2));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i), 2025, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 19 through 23 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                System.out.println("lending number already exists: " + "2024/" + seq);
                startDate = LocalDate.of(2024, 5,(i+8));
                returnedDate = LocalDate.of(2024,5,(2*i+8));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(1+i%4), 2025, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 24 through 29 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+18));
                returnedDate = LocalDate.of(2024,5,(2*i+18));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i%2+2), 2025, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 30 through 35 (not returned, not overdue)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 6,(i/3+1));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i%2+3), 2025, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                lendingRepository.save(lending);
            }
        }

        //Lendings 36 through 45 (not returned, not overdue)
        for(i = 0; i < 10; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2025/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 6,(2+i/4));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(4-i%4), 2025, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                System.out.println("Created lending: " + lending.getLendingNumber());
                System.out.println(lending.getBook().getIsbn());
                System.out.println(lending.getReaderDetails().toString());
                System.out.println(lending.getBook().getIsbn());
                lendingRepository.save(lending);

            }
        }
    }


    private void createPhotos() {
        /*Optional<Photo> photoJoao = photoRepository.findByPhotoFile("foto-joao.jpg");
        if(photoJoao.isEmpty()) {
            Photo photo = new Photo(Paths.get(""))
        }*/
    }
}
