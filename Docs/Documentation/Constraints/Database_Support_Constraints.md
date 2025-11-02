# Constraints

**Database Support Constraint**

CON-1 O sistema deve permitir a utilização de múltiplos SGBDs configuráveis via ficheiro `application.properties`.  
CON-2 O sistema deve utilizar interfaces para permitir diferentes implementações de persistência de dados.  
CON-3 A configuração do SGBD a ser utilizado deve ser possível através da anotação `@Profile` do Spring Boot, sem necessidade de alterar código.  
CON-4 O sistema deve evitar duplicação de entidades e código ao suportar múltiplos SGBDs.  
CON-5 Deve ser possível adicionar suporte a novos SGBDs no futuro de forma simples, sem afetar implementações existentes.  
CON-6 A solução deve manter compatibilidade com a stack tecnológica atual, especialmente Java e Spring Boot.  
CON-7 O sistema deve garantir integridade e consistência dos dados independentemente do SGBD selecionado.
