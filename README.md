# Directorio de Funcionarios

Aplicación web desarrollada con **Spring Boot 3**, **Java 21**, **MySQL en Aiven** y frontend con **HTML, CSS y JavaScript vanilla**.

---

## 1. Conexión con la Base de Datos MySQL en Aiven

La conexión a Aiven ya se encuentra configurada en [application.properties](file:///home/camper/Escritorio/Trabajo%20spring/funcionarios/src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:mysql://mysql-26c808f5-samuellesmes45-1d36.b.aivencloud.com:28057/defaultdb?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=avnadmin
spring.datasource.password=TU_CONTRASEÑA_AIVEN
```

> **Nota:** La tabla `funcionarios` se crea y actualiza automáticamente gracias a `spring.jpa.hibernate.ddl-auto=update`.

---

## 2. Ejecutar la Aplicación

Con el Maven Wrapper incluido:
- **Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```
- **Windows (PowerShell o CMD):**
  ```cmd
  .\mvnw.cmd spring-boot:run
  ```

Una vez iniciada:
- **Interfaz Web:** [http://localhost:8081](http://localhost:8081) *(se configuró el puerto 8081 debido a que el puerto 8080 está reservado por el Tomcat del sistema)*
- **API REST base:** [http://localhost:8081/api/v1/funcionarios](http://localhost:8081/api/v1/funcionarios)

---

## 3. Pruebas Automatizadas

Se cuenta con una suite completa de 26 pruebas unitarias y de integración que validan el 100% de los requisitos del taller:
```bash
./mvnw test
```

---

## 4. Endpoints de la API REST

| Operación | Método y ruta | Respuesta exitosa | Respuestas de error / vacío |
|---|---|---|---|
| **Listar todos** | `GET /api/v1/funcionarios` | `200 OK` (lista JSON) | `204 No Content` si está vacío |
| **Consultar por ID** | `GET /api/v1/funcionarios/{id}` | `200 OK` (objeto JSON) | `404 Not Found` si no existe / `400` si ID no es numérico |
| **Registrar** | `POST /api/v1/funcionarios` | `201 Created` (objeto con ID generado) | `400 Bad Request` si falta campo o envía ID |
| **Actualizar** | `PUT /api/v1/funcionarios/{id}` | `200 OK` (objeto actualizado) | `404 Not Found` si ID no existe / `400` si datos inválidos |
| **Eliminar** | `DELETE /api/v1/funcionarios/{id}` | `204 No Content` | `404 Not Found` si ID no existe |
| **Buscar por nombre** | `GET /api/v1/funcionarios?nombre=ana` | `200 OK` (coincidencias parciales) | `204 No Content` sin resultados |
| **Filtrar por departamento** | `GET /api/v1/funcionarios?departamento=Contabilidad` | `200 OK` (nombre completo de área) | `204 No Content` sin resultados |
