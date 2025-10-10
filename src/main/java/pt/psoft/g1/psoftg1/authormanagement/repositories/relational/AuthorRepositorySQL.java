package pt.psoft.g1.psoftg1.authormanagement.repositories.relational;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.AuthorEntity;
import pt.psoft.g1.psoftg1.authormanagement.model.relacional.BioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciamento de autores usando JPA/SQL
 * Esta interface estende CrudRepository para fornecer operações básicas de CRUD para a entidade AuthorEntity
 */
public interface AuthorRepositorySQL extends CrudRepository<AuthorEntity, Long> {

    /**
     * Busca um autor pelo seu número único de identificação
     * @param authorNumber número do autor
     * @return Optional contendo o autor se encontrado, vazio caso contrário
     */
    @Query("SELECT a FROM AuthorEntity a WHERE a.authorNumber = :authorNumber")
    Optional<AuthorEntity> findByAuthorNumber(Long authorNumber);

    /**
     * Busca autores cujo nome começa com a string fornecida
     * Útil para funcionalidades de autocompletar e pesquisa parcial
     * @param name início do nome do autor
     * @return Lista de autores que correspondem ao critério
     */
    @Query("SELECT a FROM AuthorEntity a WHERE a.name.name LIKE :name%")
    List<AuthorEntity> findByNameStartsWith(String name);

    /**
     * Busca autores com nome exatamente igual ao fornecido
     * @param name nome completo do autor
     * @return Lista de autores com o nome especificado
     */
    @Query("SELECT a FROM AuthorEntity a WHERE a.name.name = :name")
    List<AuthorEntity> searchByNameName(String name);

    /**
     * Encontra todos os coautores de um determinado autor
     * Busca autores que colaboraram em livros com o autor especificado
     * @param authorNumber número do autor para buscar coautores
     * @return Lista de autores que são coautores
     */
    @Query("SELECT DISTINCT coAuthor FROM BookEntity b " +
            "JOIN b.authors coAuthor " +
            "WHERE b IN (SELECT b FROM BookEntity b JOIN b.authors a WHERE a.authorNumber = :authorNumber) " +
            "AND coAuthor.authorNumber <> :authorNumber")
    List<AuthorEntity> findCoAuthorsByAuthorNumber(String authorNumber);

    /**
     * Retorna os autores ordenados pelo número de empréstimos de seus livros
     * Útil para relatórios de popularidade e estatísticas
     * @param pageable configuração de paginação
     * @return Page com visualização dos autores e contagem de empréstimos
     */
    @Query("SELECT new pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView(a.name.name, COUNT(l.pk)) " +
            "FROM BookEntity b " +
            "JOIN b.authors a " +
            "JOIN LendingEntity l ON l.book.pk = b.pk " +
            "GROUP BY a.name " +
            "ORDER BY COUNT(l) DESC")
    Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable);


}
