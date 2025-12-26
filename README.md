# E-commerce Project (CLI, Spring Boot + PostgreSQL)

Ett e-handelsprojekt byggt som en **konsolapplikation** ovanpå **Spring Boot 4**, **Java 21** och **PostgreSQL**.

Applikationen hanterar:

- Kunder
- Produkter & kategorier
- Lager (inventory)
- Ordrar & betalningar
- Kundvagn & checkout
- Rapporter (topp­säljare, lågt lagersaldo, omsättning)
- Import av kunder från CSV

Koden är organiserad i lager:

- `cli` – användargränssnitt i terminalen
- `services` – affärslogik
- `repository` – databasåtkomst (Spring Data JPA)
- `entity` – domänmodeller (Order, Product, Customer, osv.)
- `dto` – dataobjekt för rapporter och request/response-modeller

---

## Tekniska krav

- **Java:** 21
- **Byggsystem:** Maven (projektet innehåller `mvnw`/`mvnw.cmd`)
- **Databas:** PostgreSQL (rekommenderat 14+)
- **OS:** valfritt (Windows, macOS, Linux) – så länge Java & Postgres finns

---

## Konfiguration

Applikationen konfigurerar databasen i `src/main/resources/application.properties`:

```properties
spring.application.name=Ecommerce_project
spring.datasource.url=jdbc:postgresql://localhost:5432/e_commerce_db
spring.datasource.username=${USERNAME}
spring.datasource.password=${PASSWORD_DB}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
