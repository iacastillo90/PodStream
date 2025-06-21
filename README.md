# PodStream: Plataforma Integral de E-commerce y Gestión de Tickets

## Descripción General del Proyecto

**PodStream** es una plataforma de comercio electrónico robusta y escalable, diseñada para la venta de productos relacionados con el streaming y la producción de contenido (micrófonos, consolas de sonido, cámaras, tarjetas de sonido, accesorios, etc.). Este proyecto va más allá de un simple e-commerce, incorporando funcionalidades avanzadas de seguridad, monitoreo, integración con sistemas externos y una arquitectura modular, lo que lo convierte en un excelente caso de estudio y una solución funcional para emprendimientos.

El objetivo principal de **PodStream** es ofrecer una experiencia de compra fluida y segura, a la vez que proporciona herramientas avanzadas para la gestión interna, el monitoreo del sistema y la atención al cliente.

## Funcionalidades Principales

**PodStream** ofrece un conjunto completo de características, incluyendo:

### 🛒 **Módulo de E-commerce**
* **Gestión de Productos:**
    * Listado y consulta de productos por ID.
    * Creación, actualización y eliminación de productos (CRUD).
    * Clasificación de productos por categorías (`MICROPHONES`, `SOUND_CONSOLES`, `CAMERAS`, `SOUND_CARDS`, `ACCESSORIES`).
    * Gestión de colores disponibles para los productos.
* **Gestión de Carrito de Compras (Implícito en Pedidos):** La estructura de `Details` y `PurchaseOrder` permite la base para construir una funcionalidad de carrito.
* **Gestión de Órdenes de Compra:**
    * Creación de nuevas órdenes con validación de cliente, dirección, RUT y ticket asociado.
    * Seguimiento del estado de la orden (`PENDING_PAYMENT`, `PAYMENT_CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
    * Historial de cambios de estado de la orden.
    * Generación automática de facturas en formato PDF para cada orden, incluyendo validación de RUT chileno.
    * Envío de notificaciones por correo electrónico sobre el estado de la orden y la factura adjunta.

### 🔒 **Seguridad y Autenticación**
* **Autenticación Basada en JWT (JSON Web Tokens):**
    * Inicio de sesión y registro de usuarios mediante tokens JWT para una API REST sin estado.
    * Generación y validación de tokens JWT.
* **Control de Acceso Basado en Roles (RBAC):**
    * Definición de roles de usuario (`USER`, `ADMIN`).
    * Configuración de Spring Security para proteger endpoints basándose en los roles del usuario.
* **Cifrado de Contraseñas:** Utiliza `BCryptPasswordEncoder` para almacenar contraseñas de forma segura.

### 🗣️ **Interacción y Soporte al Cliente**
* **Sistema de Comentarios y Reseñas:**
    * Permite a los clientes comentar sobre productos específicos.
    * Soporte para respuestas a comentarios, fomentando la interacción en la plataforma.
* **Gestión de Tickets de Soporte:**
    * Creación y gestión de tickets de soporte asociados a órdenes de compra o problemas generales.
    * Registro del historial de cambios de estado de los tickets.
    * Integración con **Jira**: Automatización de la creación de issues en Jira para cada nuevo ticket de soporte, facilitando la gestión de incidencias por parte del equipo interno.
    * Notificaciones por correo electrónico sobre actualizaciones del estado de los tickets.

### 📈 **Monitoreo y Observabilidad**
* **Métricas con Spring Boot Actuator y Prometheus:**
    * Exposición de métricas de la aplicación para monitoreo en tiempo real (ej. `jira.issues.created`, `jira.issues.errors`).
    * Configuración de `prometheus.yml` para el scrape de métricas.
* **Logging Centralizado con ELK Stack (Elasticsearch, Logstash, Kibana):**
    * Configuración de `logback-spring.xml` para estructurar logs en formato JSON.
    * Uso de Logstash para procesar y enviar logs a Elasticsearch.
    * Kibana para la visualización, búsqueda y análisis de logs, facilitando la depuración y el monitoreo de la aplicación.
* **Sistema de Tickets de Monitoreo Interno (`MonitoringTicket`):**
    * Permite registrar y seguir eventos o errores específicos del sistema con detalles como `source`, `errorCode`, `severity`.
    * Mantiene un `TicketHistory` para cada `MonitoringTicket`, documentando su evolución.

### ⚙️ **Automatización y Herramientas de Desarrollo**
* **Docker y Docker Compose:**
    * Orquestación de la aplicación (Spring Boot) junto con sus dependencias (MySQL, Elasticsearch, Logstash, Kibana).
    * Entorno de desarrollo y potencial despliegue simplificado.
* **Swagger/OpenAPI (springdoc-openapi):**
    * Generación automática de documentación de la API REST interactiva, facilitando la integración con clientes frontend y otros servicios.
* **Lombok:** Reducción del código boilerplate (constructores, getters, setters, etc.), mejorando la legibilidad del código.
* **Manejo Global de Excepciones:** Centralización del manejo de errores HTTP para respuestas consistentes.
* **Validación de Datos:** Uso de `jakarta.validation` para la validación de DTOs de entrada.
* **Manejo de Transacciones:** `@Transactional` para asegurar la integridad de los datos en operaciones de base de datos.

## Estructura del Proyecto y Tecnologías

El proyecto **PodStream** está construido con una arquitectura de microservicios (o una aplicación monolítica bien modularizada, con potencial a microservicios) utilizando Spring Boot.

### 💻 **Tecnologías Backend**
* **Lenguaje de Programación:** Java
* **Framework:** Spring Boot 3.x
* **Seguridad:** Spring Security, JSON Web Tokens (JWT)
* **Base de Datos:** MySQL (con configuración HikariCP para pool de conexiones)
* **ORM / Persistencia:** Spring Data JPA, Hibernate
* **Generación de PDF:** iTextPDF
* **Envío de Correos:** JavaMailSender (integrado con Gmail SMTP)
* **Integración con Jira:** `RestTemplate` para interactuar con la API REST de Jira.
* **Métricas:** Spring Boot Actuator, Micrometer, Prometheus
* **Logging:** Logback, Logstash Encoder, Elasticsearch, Logstash, Kibana
* **Herramientas de Desarrollo:** Maven, Lombok, Springdoc-OpenAPI (Swagger)

### 🗃️ **Estructura del Código (Paquetes y Modelos Clave)**

* `com.podStream.PodStream.Configurations.Auth`: Lógica de autenticación y registro.
* `com.podStream.PodStream.Configurations.Security.Jwt`: Componentes para la gestión de JWT (filtros, servicio).
* `com.podStream.PodStream.Configurations.Security`: Configuración de Spring Security.
* `com.podStream.PodStream.Controllers`: Capa de API REST que expone los endpoints.
* `com.podStream.PodStream.Exceptions`: Manejo global de excepciones (`GlobalExceptionHandler`).
* `com.podStream.PodStream.Models`: Entidades del dominio y DTOs.
    * `Address`: Dirección de cliente/envío.
    * `Answers`: Respuestas a comentarios.
    * `ApiResponse`: Estructura estandarizada para respuestas de API.
    * `Comment`: Comentarios de productos.
    * `Details`: Detalles de línea de una orden de compra (producto, cantidad, precio).
    * `MonitoringTicket`: Tickets internos de monitoreo del sistema.
    * `PurchaseOrder`: Entidad principal de orden de compra.
    * `Product`: Entidad de producto.
    * `SupportTicket`: Tickets de soporte al cliente.
    * `TicketHistory`: Historial de estados de un ticket.
    * `User` (y subclases `Client`, `Admin`): Gestión de usuarios y roles.
    * **Enums:** `CategoryProduct`, `ColorProduct`, `OrderStatus`, `TicketStatus`, `Role`.
* `com.podStream.PodStream.Repositories`: Interfaces para la persistencia de datos (Spring Data JPA).
* `com.podStream.PodStream.Services`: Lógica de negocio (Implementaciones en `Services.Implement`).
    * `AuthService`: Servicio de autenticación.
    * `PDFService`: Generación de documentos PDF.
    * `ProductService`: Lógica de negocio para productos.
    * `PurchaseOrderService`: Lógica de negocio para órdenes de compra.
    * `SupportTicketService`: Lógica de negocio para tickets de soporte al cliente (incluye integración con Jira).
    * `TicketService`: Lógica de negocio para tickets de monitoreo.

### 🐳 **Configuración con Docker Compose**
El archivo `docker-compose.yml` define un entorno de desarrollo/producción que incluye:
* `app`: La aplicación Spring Boot, construida a partir del `Dockerfile`.
* `mysql`: Base de datos MySQL persistente.
* `elasticsearch`, `logstash`, `kibana`: El stack ELK para logging centralizado.
* `prometheus`: Servidor de monitoreo.
* `grafana`: (Potencial adición) Para visualizar métricas de Prometheus.

## Cómo Ejecutar el Proyecto (Instrucciones para Desarrollo)

1.  **Requisitos Previos:**
    * Java 17+
    * Maven
    * Docker y Docker Compose

2.  **Clonar el Repositorio:**
    ```bash
    git clone <URL_DE_TU_REPOSITORIO>
    cd PodStream
    ```

3.  **Configuración de Variables de Entorno:**
    * Crea un archivo `.env` en la raíz del proyecto o configura las variables de entorno en tu sistema/Docker Compose.
    * `DB_USERNAME`, `DB_PASSWORD`: Credenciales para la base de datos MySQL.
    * `MAIL_USERNAME`, `MAIL_PASSWORD`: Credenciales para el envío de correos (Gmail SMTP).
    * `JIRA_URL`, `JIRA_USERNAME`, `JIRA_API_TOKEN`, `JIRA_PROJECT_KEY`: Credenciales y configuración para la integración con Jira.
        * **¡ADVERTENCIA DE SEGURIDAD!** Los tokens de Jira y las contraseñas de correo electrónico están expuestas directamente en `application.properties` y `docker-compose.yml`. Para un entorno de producción, es crucial usar un gestor de secretos (Docker Secrets, HashiCorp Vault, Kubernetes Secrets, etc.) en lugar de hardcodearlas.

4.  **Iniciar los Contenedores con Docker Compose:**
    ```bash
    docker-compose up --build -d
    ```
    Esto iniciará la aplicación Spring Boot, MySQL, Elasticsearch, Logstash y Kibana.

5.  **Acceder a la Aplicación:**
    * La API REST estará disponible en `http://localhost:8088`.
    * La documentación de Swagger UI estará en `http://localhost:8088/swagger-ui.html`.
    * Kibana estará disponible en `http://localhost:5601`.
    * Prometheus estará disponible en `http://localhost:9090`.

## Estado Actual y Próximos Pasos :

### **Lo que ya se ha logrado:**
* **MVP funcional de e-commerce:** Gestión de productos y órdenes con detalles.
* **Autenticación y Autorización robusta:** Seguridad JWT y RBAC implementadas.
* **Integración vital:** PDF de facturas y envío de correos.
* **Observabilidad de primer nivel:** ELK y Prometheus operativos para monitoreo y logging.
* **Integración con herramientas de gestión:** Creación de tickets en Jira.
* **Base de datos relacional:** MySQL configurado y funcional.
* **Contenedorización:** Despliegue con Docker Compose.
* **Documentación de API:** Swagger UI para facilitar el desarrollo frontend.

### **Áreas Potenciales de Mejora y Futuras Funcionalidades:**

1.  **Frontend Desarrollado:**
    * Implementar una interfaz de usuario completa utilizando Vue.js (como mencionas en tu CV), React, Angular, o cualquier otro framework de tu elección, consumiendo esta API REST.

2.  **Optimización del Email y Notificaciones:**
    * **Dinamicidad de Destinatarios:** Modificar `SupportTicketServiceImplement.java` y `TicketServiceImplement.java` para que los correos se envíen al email del cliente asociado al ticket/orden, no a un correo hardcodeado de la tienda.
    * **Plantillas de Correo:** Utilizar plantillas (ej. con Thymeleaf, FreeMarker o incluso HTML/CSS embebido) para correos más profesionales y personalizados.
    * **Asincronía en Envío de Correos:** Implementar el envío de correos de forma asíncrona para no bloquear el hilo principal de la solicitud, mejorando la experiencia del usuario (usando `@Async` de Spring).

3.  **Refinamiento de Entidades y Relaciones:**
    * **`Client` y `Address`:** Asegurarse de que `Client` tenga una `@OneToMany List<Address>` para que un cliente pueda tener múltiples direcciones de envío/facturación, y no solo la relación `address` en `PurchaseOrder`.
    * **`SupportTicket` vs `MonitoringTicket`:** Clarificar y quizás refactorizar si `SupportTicket` puede heredar de `MonitoringTicket` o si son completamente distintos en su propósito. `OrderStatusHistory` se relaciona con `SupportTicket`, lo que sugiere que `SupportTicket` es la entidad de cara al cliente.

4.  **Pasarela de Pagos:**
    * Integrar una pasarela de pagos real (ej. Stripe, PayPal, Mercado Pago para Chile) para procesar pagos de forma segura.

5.  **Gestión de Inventario:**
    * Implementar lógica de inventario que reduzca la cantidad de productos disponibles al confirmar una compra y maneje casos de stock agotado.
    * Considerar estrategias para reservar stock temporalmente en el carrito.

6.  **Funcionalidades de Carrito de Compras:**
    * Implementar una entidad `Cart` con `CartItem` para gestionar los productos que un usuario añade antes de la compra.
    * Persistencia del carrito de compras (ej. para usuarios no logueados).

7.  **Búsqueda y Filtrado de Productos:**
    * Implementar capacidades de búsqueda avanzada (por nombre, descripción) y filtrado (por categoría, color, precio) para productos.
    * Considerar integrar Elasticsearch para búsquedas de texto completo y facetadas si el catálogo de productos crece mucho.

8.  **Gestión de Imágenes de Productos:**
    * Implementar un servicio para subir y almacenar imágenes de productos (ej. en S3 de AWS, o un almacenamiento local con una CDN).

9.  **Cacheo:**
    * Implementar cacheo (ej. con Redis o Spring Cache) para endpoints o datos frecuentemente accedidos (ej. lista de productos, datos de usuario) para mejorar el rendimiento.

10. **Internacionalización (i18n):**
    * Soporte para múltiples idiomas si el proyecto busca escalar geográficamente.

11. **Pruebas Completas:**
    * Asegurar una cobertura robusta de pruebas unitarias, de integración y end-to-end.

12. **Despliegue Continuo (CI/CD):**
    * Configurar un pipeline de CI/CD (ej. con GitHub Actions, Jenkins) para automatizar la construcción, prueba y despliegue del proyecto.

13. **Seguridad Avanzada:**
    * Rate limiting para prevenir ataques de fuerza bruta.
    * Protección CSRF si se utiliza un enfoque basado en sesiones (aunque JWT es sin estado).
    * Auditoría de seguridad (OWASP Top 10).

14. **Optimización de Logging y Monitoreo:**
    * **Alertas en Kibana/Prometheus:** Configurar reglas de alerta para eventos críticos (ej. errores de Jira, errores de correo, transacciones fallidas).
    * **Dashboards en Grafana:** Crear dashboards personalizados en Grafana (conectado a Prometheus y/o Elasticsearch) para una visualización más rica de las métricas y logs.
    * **Tracing distribuido:** Implementar un sistema de tracing (ej. Jaeger, Zipkin) para entender el flujo de las solicitudes a través de diferentes servicios si se escala a microservicios.

## Autor

**Iván Andrés Castillo Iligaray**
* **LinkedIn:** https://www.linkedin.com/in/iv%C3%A1n-castillo-03b25b243/
* **GitHub:** https://github.com/iacastillo90
* **Correo Electrónico:** iacastillo.ili2@gmail.com
