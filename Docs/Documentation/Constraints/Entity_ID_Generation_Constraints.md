# Constraints

**ID Constraint**

CON-1 O sistema deve permitir a geração de IDs em múltiplos formatos configuráveis via ficheiro `application.properties`.  
CON-2 As entidades devem utilizar IDs gerados dinamicamente, substituindo atributos de identificação fixos.  
CON-3 Deve ser possível implementar novos formatos de ID através de classes que implementem a interface de geração de IDs.  
CON-4 O sistema deve garantir que todos os IDs gerados sejam únicos e compatíveis com o sistema de persistência.  
CON-5 O tempo de geração de IDs não deve impactar a performance geral do sistema de forma significativa.  
CON-6 A implementação deve ser compatível com a stack tecnológica atual, especialmente Java.  
CON-7 A solução deve permitir fácil manutenção e adição de novos formatos sem afetar a lógica de negócio existente.
