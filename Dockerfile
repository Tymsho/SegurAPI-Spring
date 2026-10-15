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

# Crear usuario y grupo sin privilegios para no correr como root
RUN addgroup -S spring && adduser -S spring -G spring

# Copiamos el jar compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Aseguramos que la carpeta para uploads exista y asignamos permisos al usuario spring
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Ejecutar como usuario no privilegiado
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
