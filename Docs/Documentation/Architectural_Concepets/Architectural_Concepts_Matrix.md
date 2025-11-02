# Architectural Concepts

| Requirement / Driver | Concept / Pattern / Style alternatives | Selection |
|---------------------|----------------------------------------|-----------|
| **ID Generation**: Flexibility to support multiple ID formats | 1. Hard-coded generation<br>2. ID Generator Interface + Multiple Implementations<br>3. ID Generator Microservice<br>4. Configurable via properties file | 2 + 4 |
| **ID Generation**: Consistency and uniqueness | 1. Simple UUID<br>2. Database auto-increment<br>3. Centralized ID Generator Service | 3 |
| **ISBN Retrieval**: Automatic retrieval from external services | 1. Manual entry<br>2. Tight coupling with external services<br>3. Service Layer + Configurable Implementations | 3 |
| **ISBN Retrieval**: Extensibility to support new services | 1. Hard-coded API integration<br>2. Service Interface + New implementations<br>3. Event-driven integration | 2 |
| **Multi-DB Support**: Adaptation to different clients' DB needs | 1. Separate modules per SGBD<br>2. Persistence Abstraction Layer + Interfaces<br>3. Profiles via Spring Boot @Profile | 2 + 3 |
| **Multi-DB Support**: Data integrity across DBs | 1. ACID transactions only<br>2. BASE transactions<br>3. Saga pattern for distributed consistency | 3 |
| **Multi-DB Support**: Ease of adding new SGBDs | 1. Duplicate entities per DB<br>2. Use of generic repository interfaces<br>3. Configurable via properties file | 2 + 3 |
