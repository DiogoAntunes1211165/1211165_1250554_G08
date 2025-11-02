# Library Management System – Technical Memos

---

## Memo 1: ISBN Lookup via External APIs

### Issue
Criação de livros sem ISBN fornecido e necessidade de obter automaticamente um ISBN válido por título/autores usando fornecedores externos (OpenLibrary, Google Books).

### Problem
A dependência de APIs externas introduz fragilidade: latência, indisponibilidade, resultados inconsistentes e formatos diferentes que podem causar falhas na criação de livros ou inserir dados incorretos.

### Summary of solution
Implementar uma estratégia de fallback e resiliência com beans ordenados que consultam fornecedores em sequência, aplicando táticas ADD: evitar chamadas desnecessárias, detetar falhas cedo e usando cache e políticas de retry/circuit-breaker.

### Factors
- Dependência de conectividade HTTP e credenciais/quotas dos providers.
- Ordem de prioridade entre providers.
- Requisitos de latência aceitável para criação de recursos.
- Dados incompletos ou conflito de metadados entre providers.

### Solution
- Reduce Coupling - Isolar a geração de ISBN do fluxo principal de criação de livros
- Use an Intermediary - Utilizar um serviço de lookup dedicado
- Abstract Common Services - Definição de uma interface comum para lookups de ISBN
- Defer Binding - Configuração de beans ordenados para providers externos

### Motivation
Reduzir falhas de criação de livros, limitar o impacto de fornecedores externos e manter a experiência do utilizador previsível, garantindo que o sistema degrade de forma segura quando APIs externas falharem.

### Alternatives
- Dependência exclusiva de um único provider (mais simples, mas menos resiliente).
- Fazer todos os lookups de forma assíncrona após criação do registo (menor latência na criação, mas complexidade operacional e eventual consistência).

---

## Memo 2: ID Generation Flexibility

### Issue
Necessidade de suportar múltiplas estratégias de geração de IDs (por exemplo: `base65`, `timestamp_hex`) configuráveis por ambiente para atender a requisitos de legibilidade, compatibilidade e segurança.

### Problem
Trocar a estratégia de geração de IDs pode causar colisões, incompatibilidades com dados existentes e problemas de rastreabilidade; exigir mecanismo seguro e transparente para alternância e migração.

### Summary of solution
Definir uma interface `IdGenerator` com implementação por perfil e um `IdGeneratorFactory`. Aplicar táticas ADD para evitar colisões, detetar problemas de unicidade e defender com fallbacks e políticas de migração.

### Factors
- IDs existentes no sistema e expectativas dos consumidores (formato, tamanho, caracteres permitidos).
- Requisitos de segurança (não expor timestamps claros, resistir a enumeração).
- Desempenho e escalabilidade na geração em ambientes concorrentes.

### Solution
- Reduce Coupling - Geração de IDs desacoplada do restante do sistema
- Use an Intermediary - Serviço de intermediação entre a geração de IDs e o restante do sistema
- Abstract Common Services - Implementação de interface comum para geração de IDs, com implementações variadas
- Defer Binding - Configuração por perfil e fábrica para seleção dinâmica da estratégia

### Motivation
Permitir flexibilidade operacional e requisitos de produto sem comprometer integridade dos dados ou necessidade de ramificações de código; facilitar testes e rollback de estratégias.

### Alternatives
- Usar UUIDs universais como padrão.
- Adotar um serviço central de geração de IDs.

---

## Memo 3: Supporting Multiple DBMS through Configuration

### Issue
Capacidade de usar diferentes tipos de DBMS, relacional e Document.

### Problem
Diferenças no modelo de dados e APIs entre DBMS levam a duplicação de código e dificultam manutenção. Devemos permitir troca por configuração em ambiente de produção/CI, minimizando riscos.

### Summary of solution
Arquitetura por abstração da persistência (repositórios/ports + adapters), perfis Spring para selecionar implementações e um conjunto de táticas ADD para reduzir o risco operacional: evitar uso de features específicas dos DBMS quando possível, detetar incompatibilidades via testes automatizados e defender com adaptadores e políticas de fallback.

### Factors
- Necessidade de suportar modelos relacionais (SQL Server) e document (MongoDB) e caches (Redis).
- Mapeamentos de entidades e consultas que podem não ser triviais de traduzir entre modelos.
- Requisitos de integridade transacional e atomicidade.

### Solution
- Reduce Coupling - Sistema de persistência desacoplado do restante da aplicação
- Use an Intermediary - Abstrair a camada de persistência com repositórios
- Abstract Common Services - Permitir múltiplas implementações de repositórios para diferentes DBMS
- Defer Binding - Injeção de dependências baseada em perfis de configuração

### Motivation
Reduzir duplicação de código e custo de manutenção, permitir deploys em ambientes heterogéneos e responder a requisitos do product owner sem forks. Tornar a troca de DBMS uma operação de configuração e validação, não de desenvolvimento intenso.

### Alternatives
- Suportar apenas um DBMS oficial e manter adaptadores comunitários (mais simples, menos flexível).
- Camada de persistência externa (DBaaS/abstração comercial) — reduz esforço, mas adiciona custo e dependência.

---
