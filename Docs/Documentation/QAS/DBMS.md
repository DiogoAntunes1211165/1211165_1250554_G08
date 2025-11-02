# Quality Attribute Scenario: Suporte a Múltiplos Sistemas de Base de Dados via Configuração

| Elemento        | Declaração sobre a necessidade de flexibilidade na utilização de bases de dados.                                                                                                                      |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Stimulus        | Existe a impossibilidade de utilizar diferentes tipos de Sistemas de Gestão de Base de Dados (DBMS) quando solicitado pelo responsável do produto.                                              |
| Fonte do Stimulus | O responsável do produto necessita de utilizar vários tipos de DBMS.                                                                                                                             |
| Ambiente        | O requisito do responsável do produto de usar diferentes tipos de DBMS exige atualmente a manutenção de ramificações ou projetos separados, aumentando o risco de duplicação de código e complicando a manutenção. |
| Artefacto       | O software LMS, especificamente a sua camada de persistência.                                                                                                                                  |
| Resposta        | Permitir a persistência de dados em múltiplos modelos de dados (por exemplo, relacional e documento) e diferentes DBMS (por exemplo, MySQL, SQL Server, MongoDB, Redis) utilizando interfaces para diferentes implementações. Utilizar a anotação `@Profile` do Spring Boot para configurar dinamicamente o DBMS ativo com base no ambiente. |
| Métrica da Resposta | Deve ser possível alterar o modelo de dados e o DBMS utilizado apenas através da configuração, em menos de 30 minutos.                                                                       |

---
