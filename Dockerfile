# Etapa 1: Build
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

# Copiamos archivos de configuración y wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Damos permisos de ejecución a mvnw
RUN chmod +x ./mvnw

# Construimos el proyecto omitiendo los tests
RUN ./mvnw clean package -DskipTests

# Etapa 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el jar compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Aseguramos que la carpeta para uploads exista (por configuración local)
RUN mkdir -p uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
