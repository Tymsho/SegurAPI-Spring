# 🛡️ SegurAPI — Sistema de Gestión de Pólizas y Cartera de Seguros

API RESTful desarrollada con **Java 17** y **Spring Boot 3.5**, diseñada para la gestión integral de carteras de clientes, pólizas, compañías aseguradoras, importación de datos y notificaciones para Productores Asesores de Seguros.

---

## ⚙️ Arquitectura y Características Principales

* **Arquitectura en Capas:** Separación estricta entre Controladores (`REST`), Servicios (lógica de negocio), Repositorios (`Spring Data JPA`) y transferencia de datos mediante `DTOs` (Requests/Responses).
* **Seguridad Stateless (JWT):** Autenticación basada en JSON Web Tokens, encriptación de contraseñas con `BCrypt`, configuración dinámica de CORS y control de acceso basado en roles (`ADMIN` y `PRODUCTOR`).
* **Base de Datos Cloud-Ready (PostgreSQL):** Configurada para **PostgreSQL / Supabase** con el pool de conexiones **HikariCP** optimizado para entornos cloud y serverless.
* **Módulos del Sistema:**
  * Gestión de **Clientes** y **Pólizas** con filtros avanzados y consultas personalizadas (`JPQL`).
  * Catálogo preconfigurado de **Compañías Aseguradoras**, **Ramos** y **Geografía** (Provincias y Localidades).
  * Gestión de **Plantillas de Importación** y sistema de **Notificaciones** vía correo electrónico (`SMTP`).
* **Manejo Centralizado de Errores:** Validaciones declarativas (`@Valid`) y captura global mediante `GlobalExceptionHandler` con respuestas HTTP estandarizadas.
* **Testing Automatizado:** Pruebas unitarias y de integración en capas de servicio y controladores con `JUnit 5`, `Mockito` y `MockMvc`.

---

## 🛠️ Stack Tecnológico

* **Lenguaje y Framework:** Java 17, Spring Boot 3.5.x
* **Persistencia:** Spring Data JPA, Hibernate, PostgreSQL Driver, HikariCP
* **Seguridad:** Spring Security, JJWT (JSON Web Token)
* **Notificaciones:** Spring Boot Starter Mail (SMTP)
* **Testing:** JUnit 5, Mockito, MockMvc

---

## 🚀 Configuración y Variables de Entorno

El proyecto soporta la carga nativa de archivos `.env` en la raíz del proyecto o variables de entorno inyectadas desde el servidor de despliegue.

Creá un archivo `.env` en la raíz del repositorio basándote en la siguiente estructura:

```properties
# Servidor
PORT=8080

# Base de Datos (PostgreSQL / Supabase Session Pooler)
DB_URL=jdbc:postgresql://[aws-0-sa-east-1.pooler.supabase.com:5432/postgres?prepareThreshold=0&sslmode=require](https://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?prepareThreshold=0&sslmode=require)
DB_USERNAME=postgres.tu_referencia_proyecto
DB_PASSWORD=tu_password_seguro
DB_POOL_SIZE=5

# Hibernate / JPA
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# Seguridad JWT (Mínimo 32 bytes / 256 bits)
JWT_SECRET=TuClaveSecretaDeProduccionConMasDe32BytesDeLongitud
JWT_EXPIRATION=86400000

# CORS (Orígenes permitidos separados por coma)
CORS_ALLOWED_ORIGINS=http://localhost:5173,[https://tu-frontend.vercel.app](https://tu-frontend.vercel.app)

# Seeder y Usuario Administrador Inicial
SEEDER_ENABLED=true
ADMIN_EMAIL=tu_email@dominio.com
ADMIN_NOMBRE=Administrador
ADMIN_PASSWORD=TuPasswordAdminSeguro

# Servidor de Correo (SMTP Gmail)
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_app_password_de_gmail
```

---

## 💻 Ejecución Local

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/tu-usuario/SegurAPI-Spring.git](https://github.com/tu-usuario/SegurAPI-Spring.git)
   cd SegurAPI-Spring
   ```
2. **Configurar el entorno:**
   Crear el archivo `.env` con las credenciales de tu instancia local de PostgreSQL o tu proyecto de Supabase.
3. **Ejecutar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```
   La API quedará disponible en `http://localhost:8080`.

---

## 🌱 Inicialización de Datos (`DataSeeder`)

Al iniciar la aplicación con `SEEDER_ENABLED=true`, el componente `DataSeeder.java` verifica e inicializa de forma idempotente únicamente los catálogos base:
1. **Geografía inicial:** Provincias y localidades base (si la tabla está vacía).
2. **Catálogo oficial:** 37 compañías aseguradoras y 10 ramos principales.
3. **Usuario Administrador:** Si no existe un usuario con el correo definido en `ADMIN_EMAIL`, lo crea automáticamente con rol `ADMIN` y la contraseña definida en `ADMIN_PASSWORD` encriptada con `BCrypt`. No se inyectan clientes ni pólizas de prueba ni credenciales por defecto.

---

## 🔑 Autenticación y Uso de la API

Para consumir los endpoints protegidos, realizá una petición `POST` a `/api/auth/login` enviando las credenciales configuradas en tus variables de entorno:

```json
{
  "email": "tu_email@dominio.com",
  "password": "TuPasswordAdminSeguro"
}
```

La respuesta devolverá el token JWT, que debés incluir en el header de las siguientes peticiones:
```http
Authorization: Bearer <tu_token_jwt>
```
