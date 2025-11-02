# Constraints

**ID Constraint**

CON-1 O sistema deve permitir a integração com múltiplos serviços externos de ISBN configuráveis via ficheiro `application.properties`.  
CON-2 O usuário não deve precisar inserir manualmente o ISBN ao adicionar um novo livro.  
CON-3 O sistema deve suportar falhas temporárias nos serviços externos (timeouts ou indisponibilidade), retornando mensagens de erro amigáveis.  
CON-4 Deve ser possível alterar o serviço externo a ser utilizado apenas alterando o ficheiro de configuração, sem necessidade de recompilar ou reiniciar o sistema.  
CON-5 O sistema deve manter compatibilidade com os principais serviços externos de ISBN atualmente disponíveis no mercado.  
CON-6 Logs de consultas externas devem ser armazenados por pelo menos 30 dias para auditoria.  
CON-7 A implementação deve ser feita utilizando tecnologias compatíveis com o ambiente Java da aplicação existente.  
CON-8 O sistema deve validar os ISBN retornados para garantir que são válidos antes de salvar no banco de dados.  
CON-9 Futuras integrações com novos serviços de ISBN devem ser facilmente adicionáveis via implementação de novas classes de serviço.
