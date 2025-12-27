# 🚀E-commerce Project (CLI, Spring Boot + PostgreSQL)

Ett e-handelsprojekt byggt som en **konsolapplikation** ovanpå **Spring Boot 4**, **Java 21** och **PostgreSQL**.

Applikationen hanterar:

- Kunder
- Produkter & kategorier
- Lager (inventory)
- Ordrar & betalningar
- Kundvagn & checkout
- Rapporter (topp­säljare, lågt lagersaldo, omsättning)
- Import av kunder från CSV

## Projektstruktur & arkitektur

Projektet är en **lagerindelad Spring Boot-applikation** där **CLI (terminalmenyer)** fungerar som presentationslager (ingen REST/GUI). Målet är att hålla koden tydlig, testbar och lätt att utöka: CLI → Services → Repositories → Entities.

---

## Arkitektur i korthet

- **CLI (Presentation layer)**  
  Tar emot användarinput, visar menyer och anropar services. Innehåller ingen affärslogik.

- **Service layer (Business logic)**  
  Affärsregler och transaktioner (`@Transactional`). Orkestrerar flöden (t.ex. checkout, lager, betalning).

- **Repository layer (Persistence)**  
  Spring Data JPA repositories för DB-access.

- **Entity layer (Domain model)**  
  JPA-entities och relationer (Product, Category, Order, Payment, Inventory …).

- **DTO / Import / Exceptions**
    - DTO: små request/response-objekt för service/CLI
    - CSV-import: bulkdata för simulering
    - Exception: centraliserade fel (t.ex. `AppException`) med validation/business/not found

---

## Bilde av systemet (lager)

```text
┌───────────────────────────┐
│           CLI             │
│  Menyer + input/output    │
└─────────────┬─────────────┘
              │
              v
┌───────────────────────────┐
│         Services          │
│ Affärslogik + @Transactional│
└─────────────┬─────────────┘
              │
              v
┌───────────────────────────┐
│       Repositories        │
│  Spring Data JPA (DB)     │
└─────────────┬─────────────┘
              │
              v
┌───────────────────────────┐
│          Entities         │
│   JPA-modell + relationer │
└───────────────────────────┘
```
---
### Projektstruktur (paket/träd)
```
src/
├─ main/
│   ├─ java/
│   │   └─ org/example/ecommerce_project/
│   │       ├─ cli/
│   │       │   ├─ (huvudmeny/router, t.ex. ConsoleApp/AppRouter)
│   │       │   ├─ ProductCli
│   │       │   ├─ CustomerCli
│   │       │   ├─ OrderCli
│   │       │   ├─ CartCli
│   │       │   ├─ ImportCli
│   │       │   └─ (övriga CLI-moduler)
│   │       │
│   │       ├─ services/
│   │       │   ├─ ProductService
│   │       │   ├─ CategoryService
│   │       │   ├─ CustomerService
│   │       │   ├─ InventoryService
│   │       │   ├─ OrderService
│   │       │   ├─ CheckoutService
│   │       │   ├─ PaymentService
│   │       │   └─ (rapporter, hjälp-services)
│   │       │
│   │       ├─ services/csv_import/
│   │       │   ├─ BulkImportService
│   │       │   ├─ CsvUtil
│   │       │   └─ ImportReport
│   │       │
│   │       ├─ repository/
│   │       │   ├─ ProductRepository
│   │       │   ├─ CategoryRepository
│   │       │   ├─ CustomerRepository
│   │       │   ├─ OrderRepository
│   │       │   └─ (övriga repositories)
│   │       │
│   │       ├─ entity/
│   │       │   ├─ Product
│   │       │   ├─ Category
│   │       │   ├─ Inventory
│   │       │   ├─ Order
│   │       │   ├─ OrderItem
│   │       │   ├─ Payment
│   │       │   └─ (övriga entities)
│   │       │
│   │       ├─ dto/
│   │       │   └─ (request/response-objekt, t.ex. OrderItemRequest)
│   │       │
│   │       └─ exception/
│   │       |    ├─ AppException
│   │       |    └─ (feltyper/handlers)
|   |       |
|   |       └─ data    
|   |           └─ import/
│   |               ├─ products_*.csv
│   |               ├─ categories.csv
│   |               └─ customers_*.csv
│   │
│   └─ resources/
│       ├─ application.properties
|           ├─ generate_customers.sql
|           ├─ generate_order.sql
|           └─ generate_products.sql
│      
│
└─ test/
├─ java/ (unit + integration tests)
└─ resources/
└─ application.properties (test-konfiguration)
```
### Domänmodell (relationer – översikt)
```
Product 1 ── 1 Inventory
Product * ── * Category   (join table: product_category)
Order   1 ── * OrderItem
OrderItem * ── 1 Product
Order   1 ── 1 Payment   (om ni valt 1-1 i modellen)
Customer 1 ── * Order
```

### Notering om Many-to-Many (Product–Category):

* Product är owning side (har @JoinTable) och styr skrivning till product_category.
* För stabilt beteende med Set<Category> bör Category ha equals/hashCode (t.ex. baserat på name om name är unique).

---

## Dataimport (CSV) – syfte & placering

CSV-import används för att snabbt skapa dataset och simulera scenarion (många produkter/kunder/kategorier).

* **Importlogik:** services/csv_import/BulkImportService
* **CSV-läsning:** CsvUtil
* **Resultat:** ImportReport (total/success/failed + felrader)

**Rekommenderat:**
* Lägg CSV-filer i src/main/resources/import/ för enkel test/demo
* Alternativt en extern mapp som data/import/ om ni vill kunna byta filer utan rebuild
---
### Exempel på flöde (Checkout)

1. CLI samlar input (kund, varor, betalmetod)
2. CheckoutService koordinerar:
   * hämtar kund och produkter 
   * reserverar lager via InventoryService
   * skapar order via OrderService
   * simulerar betalning via PaymentService
3. Repositories sparar i PostgreSQL
4. CLI skriver ut order-id och status, eller tydligt fel via AppException
---
### Teststrategi (kort)

* **Unit tests:** services med Mockito (snabba, isolerade)
* **Integration tests:** JPA + DB-setup vid behov (verifierar mapping, join tables, queries)
* Målet är att affärsregler (t.ex. lagerreservering) testas så att inga delvis uppdaterade data lämnas kvar vid failure.
## Tekniska krav

- **Java:** 21
- **Byggsystem:** Maven (projektet innehåller `mvnw`/`mvnw.cmd`)
- **Databas:** PostgreSQL (rekommenderat 14+)
- **OS:** valfritt (Windows, macOS, Linux) – så länge Java & Postgres finns

---

## ⌛️ Körning & test (IntelliJ – en gång)

### 1) Lägg in miljövariabler i IntelliJ (Run Configuration)
Gå till: **Run → Edit Configurations…**

- För **Spring Boot / mvn spring-boot:run**:
    - Öppna din run config
    - Fältet **Environment variables**:
        - `USERNAME=postgres;PASSWORD_DB=postgres`

- För **JUnit / mvn test** (om du kör tester via IntelliJ):
    - Öppna din test config (JUnit)
    - **Environment variables**:
        - `USERNAME=postgres;PASSWORD_DB=postgres`

### 2) Starta applikationen
* Kör din Spring Boot run configuration.

**ELLER**

* mvn spring-boot:run

### 3) Kör tester
* Kör testerna (JUnit i IntelliJ)

**ELLER**

* mvn test
