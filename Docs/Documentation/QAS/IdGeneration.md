# Quality Attribute Scenario: Flexibilidade na Geração de IDs

| Elemento        | Declaração sobre a necessidade de geração de IDs.                                                                                 |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------|
| Stimulus        | Criação de uma nova entidade                                                                                                     |
| Fonte do Stimulus | Caminho de código responsável pela criação das entidades                                                                      |
| Ambiente        | Sistema em execução (runtime)                                                                                                     |
| Artefacto       | `IdGenerator` (interface), `IdGeneratorFactory`, implementações: `IdGeneratorBase65EncodeImpl`, `IdGeneratorTimestampHexImpl`  |
| Resposta        | Dependendo do perfil escolhido (`base65` ou `timestamp_hex`), o ID é gerado                                                      |
