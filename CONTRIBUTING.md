# Contributing to Jobby ERP 🚀

First of all, thank you for considering contributing to Jobby ERP! It's people like you who make open-source a great tool for the Colombian developer community.

As a project focused on **Electronic Invoicing**, technical precision and architectural consistency are our top priorities.

---

## 🛠️ Getting Started

### 1. Prerequisites
Since this is a microservices-based project, you will need:
* **Java 21+** (for Spring Boot, Quarkus, and Micronaut services).
* **Python 3.10+** (for ETL and OLAP processing).
* **Docker & Docker Compose** (to run infrastructure like PostgreSQL, Redis, and RabbitMQ).
* **Maven** or **Gradle** (depending on the specific service).

### 2. Local Setup

1. **Fork** the repository on GitHub.

2. **Clone** your fork locally:

```bash
git clone [https://github.com/YOUR_USER/jobby-erp-backend.git](https://github.com/YOUR_USER/jobby-erp-backend.git)
```

3. Create a **feature branch**:

```bash
git checkout -b feature/amazing-feature
```

---

## 📝 Contribution Rules

### Code Style & Standards

- **Microservices:** Each service must remain independent. Avoid shared databases between services.

- **Documentation:** Any new feature or API endpoint must be documented in the code (Swagger/OpenAPI for Java).

- **Language:** Please use **English** for code (classes, variables, methods) and **Spanish or English** for documentation and commit messages.


### Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` for new features.

- `fix:` for bug fixes.

- `docs:` for documentation changes.

- `refactor:` for code changes that neither fix a bug nor add a feature.

-  `chore:` tareas rutinarias que no sean específicas de una feature o un error como por ejemplo añadir contenido al fichero `.gitignore` o instalar una dependencia.

- `test:` si añadimos o arreglamos tests.

- `build:` cuando el cambio afecta al compilado del proyecto.

- `ci:` el cambio afecta a ficheros de configuración y scripts relacionados con la integración continua.

- `style:` cambios de legibilidad o formateo de código que no afecta a funcionalidad.

- `refactor:` cambio de código que no corrige errores ni añade funcionalidad, pero mejora el código.

- `perf:` usado para mejoras de rendimiento.

- `revert:` si el commit revierte un commit anterior. Debería indicarse el hash del commit que se revierte.


---

## 🏗️ How to Help

We are specifically looking for help in:

1. **DIAN Integration:** Implementing XML signing and UBL 2.1 validation.

2. **Microservices Communication:** Improving event-driven patterns between Spring Boot and Quarkus.

3. **Data Analysis:** Developing OLAP cubes with Python for real-time financial dashboards.

4. **Testing:** Increasing unit and integration test coverage.


---

## 🐞 Reporting Issues

If you find a bug or have a suggestion:

1. Check the [Issues Tab](https://www.google.com/search?q=https://github.com/AmpolStack/jobby-erp-backend/issues) to see if it has already been reported.

2. If not, open a new issue using a clear title and a detailed description of the problem or feature request.


---

## ⚖️ License

By contributing to Jobby ERP, you agree that your contributions will be licensed under the project's **MIT License**.

**Happy coding!** 🇨🇴