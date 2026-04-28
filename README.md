# SkillShare

SkillShare is a peer-to-peer skill exchange web application where users can offer skills, request skills they want to learn, message other users, and complete exchanges with a rating system.

The main application is a Spring Boot project located in `Skill-Share/`.

## Features
- **User management**
  - Registration and login
  - Profile management
- **Skill management**
  - Offer skills
  - Browse and request skills
- **Skill exchange workflow**
  - Send / accept / reject requests
  - Track progress and history
- **Messaging & notifications**
  - Direct user-to-user messaging
  - Notifications for important updates
- **Ratings**
  - Mutual rating after completion

## Tech Stack
- **Backend**
  - Java 17
  - Spring Boot
  - Spring MVC, Spring Security
  - Spring Data JPA (Hibernate)
- **Frontend**
  - Thymeleaf templates
  - HTML/CSS
- **Database**
  - MySQL
- **Testing**
  - Cypress (UI testing)

## Prerequisites
- Java 17 (JDK)
- MySQL Server
- Git

Note: Maven is not required because the project includes the Maven Wrapper (`mvnw.cmd`).

## Local Setup (Windows)

### 1) Create the database
Create a MySQL database named `skillshare_db`.

```sql
CREATE DATABASE skillshare_db;
```

### 2) Configure database credentials
The application reads DB settings from environment variables (with sensible defaults). The defaults are defined in:

`Skill-Share/src/main/resources/application.properties`

Required variables (recommended):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Default values (if not set):
- URL: `jdbc:mysql://localhost:3306/skillshare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- Username: `root`
- Password: (empty)

Server port:
- `PORT` (default `8085`)

### 3) Run the application
From the `Skill-Share/` folder:

```bat
.\mvnw.cmd spring-boot:run
```

If you want to run with explicit env vars (example credentials):

```bat
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=root123
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/skillshare_db?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
set PORT=8085
.\mvnw.cmd spring-boot:run
```

Then open:
- `http://localhost:8085/`

## Running Cypress Tests
Cypress configuration and dependencies are in `Skill-Share/package.json`.

From the `Skill-Share/` folder:

```bat
npm install
npm run cypress:open
```

Or run headless:

```bat
npm run cypress:run
```

## Troubleshooting
- **Port already in use**
  - Change `PORT` (or free the port), then restart the app.
- **Database connection fails**
  - Ensure MySQL is running.
  - Ensure `skillshare_db` exists.
  - Confirm `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`.
- **App starts but pages don’t update**
  - Hard refresh the browser:
    - Chrome/Edge: `Ctrl+F5` or `Ctrl+Shift+R`
