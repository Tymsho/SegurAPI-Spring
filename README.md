# 🛡️ API Principal: Sistema de Gestión de Pólizas de Seguros

Esta es la API principal del trabajo práctico, desarrollada en **Spring Boot (Java)**. Implementa una arquitectura en capas completa para gestionar clientes, pólizas, compañías aseguradoras y ramos.

## ⚙️ Características y Requisitos Cumplidos
* **Arquitectura en Capas:** Controladores, Servicios, Repositorios y uso estricto de **DTOs** para Requests y Responses.
* **Seguridad (JWT):** Autenticación Stateless basada en **JSON Web Tokens** y protección de rutas por roles (`ADMIN` y `USER`).
* **Base de Datos Relacional:** Configurada para MySQL (`first_api`) mediante Spring Data JPA. Incluye relaciones complejas (`OneToMany`, `ManyToOne`, `ManyToMany`).
* **Consultas Personalizadas:** Implementación de filtrado mediante Query Params y sentencias JPQL (`@Query`).
* **Manejo de Excepciones:** Validaciones (`@Valid`) y un `GlobalExceptionHandler` para capturar errores y retornar respuestas HTTP adecuadas.
* **Testing:** Cobertura de pruebas en Servicios y Controladores utilizando JUnit, Mockito y MockMvc.

## 🚀 Ejecución y Pruebas
1. Clonar el repositorio.
2. Crear una base de datos vacía en MySQL llamada `first_api` o dejar que Hibernate la cree según la configuración local.
3. Configurar las credenciales de MySQL en `src/main/resources/application.properties`.
4. Ejecutar la aplicación (por defecto se levantará en el puerto **`8080`**).

> **Nota importante:** La estructura DDL se generará automáticamente. Al arrancar, el componente `DataSeeder.java` inyectará de forma automática las compañías, ramos, roles y usuarios semilla con sus contraseñas encriptadas mediante BCrypt.

## 🔑 Credenciales de Acceso
Para probar los endpoints, se debe realizar un `POST` a `/api/auth/login` con las siguientes credenciales para obtener el Token JWT (Bearer Token):

* **Administrador** (Acceso total: GET, POST, PUT, DELETE):
  * Email: `admin@mail.com`
  * Password: `admin123`
* **Productor** (Acceso de solo lectura: GET):
  * Email: `productor@seguros.com`
  * Password: `clave123`
