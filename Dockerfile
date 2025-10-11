# Base image com JDK 17 leve
FROM eclipse-temurin:17-jdk-alpine

# Diretório de trabalho dentro do container
WORKDIR /app

# Copiar o JAR gerado pelo Jenkins
COPY target/psoft-g1-0.0.1-SNAPSHOT.jar app.jar

# Definir perfil ativo (pode ser sobrescrito via variável de ambiente)
ENV SPRING_PROFILES_ACTIVE=dev

# Expor porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
