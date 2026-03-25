# IPOS-PU — InfoPharma Public Ordering Portal

A JavaFX desktop application allowing members of the public to browse a pharmaceutical product catalogue, place orders, and track their deliveries. Part of the IPOS (InfoPharma Ordering System) suite, integrating with IPOS-SA and IPOS-CA.

---

## Prerequisites

Before running the application, make sure you have the following installed:

- **Java 21** or higher — [Download from Oracle](https://www.oracle.com/java/technologies/downloads/) or use any JDK distribution
- **Git** — to clone the repository

No separate Maven installation is needed — the project includes a Maven wrapper (`mvnw`).

To check your Java version:
```
java -version
```

---

## Getting Started

### 1. Clone the repository

```
git clone https://github.com/iOrderTeam/IPOS.git
cd IPOS
```

### 2. Switch to the correct branch

```
git checkout feature/catalogue-cart-orders
```

### 3. Run the application

**On Mac/Linux:**
```
./mvnw javafx:run
```

**On Windows:**
```
mvnw.cmd javafx:run
```

The desktop window will open automatically.

---

## First Time Setup

The application uses an embedded H2 database — no database installation is required. The database is created automatically on first run.

To get started in the app:

1. Click **Register** on the home screen
2. Choose **Non-Commercial** membership
3. Enter your email address — your login credentials will be shown on screen
4. Log in and you will be prompted to change your password
5. Once logged in, click **View Catalogue** to browse products

---

## Accessing the Database (Optional)

The H2 database console is available at:

```
http://localhost:8080/h2-console
```

Connection settings:
- **JDBC URL:** `jdbc:h2:file:./ipospu-db`
- **Username:** `sa`
- **Password:** *(leave blank)*

---

## Project Structure

```
src/main/java/com/ipos/pu/
    model/          - JPA entity classes (Member, Product, Order, etc.)
    repository/     - Spring Data JPA repositories
    service/        - Business logic
    controller/     - REST API endpoints
    ui/controller/  - JavaFX screen controllers
src/main/resources/
    com/ipos/pu/ui/ - FXML layout files for each screen
    application.properties
```

---

## Troubleshooting

**Application window does not open**
- Ensure you are running Java 21+. Java 23 is also confirmed to work.
- On Mac, if you see a security warning, go to System Settings > Privacy & Security and allow the app to run.

**`./mvnw` permission denied (Mac/Linux)**
```
chmod +x mvnw
./mvnw javafx:run
```

**Port 8080 already in use**
- Another application is using port 8080. Stop it, or change the port in `src/main/resources/application.properties`:
```
server.port=8081
```
