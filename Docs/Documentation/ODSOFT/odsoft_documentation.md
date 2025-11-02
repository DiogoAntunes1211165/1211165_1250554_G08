# Análise Crítica do Pipeline Jenkins

## Estrutura Geral

O pipeline está bem organizado, seguindo um fluxo lógico com os seguintes passos:

- Limpeza do workspace.
- Checkout do repositório na branch `staging`.
- Execução de testes unitários com cobertura JaCoCo.
- Execução de testes de mutação com PIT.
- Realização de análise com SonarQube.
- Verificação do Quality Gate.
- Build do JAR do projeto.
- Execução de testes de integração.
- Build da imagem Docker.
- Deploy para o ambiente de staging.

A utilização do Docker para isolar a execução do Maven garante consistência no ambiente, o que é um aspeto positivo.

## Pontos Fortes

- **Uso de Docker**: Garante consistência entre os diferentes ambientes de build.
- **Separação clara das etapas**: Cada etapa tem uma responsabilidade bem definida.
- **Análise de qualidade e cobertura**: Integra SonarQube e JaCoCo, essencial para manter a qualidade do código.
- **Testes de mutação**: A inclusão de testes de mutação demonstra atenção à qualidade e robustez dos testes.
- **Quality Gate**: O pipeline aguarda a validação do SonarQube antes de prosseguir, prevenindo builds problemáticos.
- **Deploy automatizado**: Utiliza `docker-compose` para atualizar automaticamente o ambiente de staging.
